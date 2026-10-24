package app.buckykeys.keyboard.ime.engine

import android.view.KeyEvent
import app.buckykeys.keyboard.ime.geometry.KeyboardGeometry
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.ModifierKind

public class KeyboardEngine(private val geometry: KeyboardGeometry) {

    private val pointerKeys = IntArray(MAX_POINTERS) { NO_KEY }

    private var oneShotMask = 0
    private var heldMask = 0
    private var chordedMask = 0

    public var lockedMask: Int = 0
        private set

    public val modifierMask: Int
        get() = oneShotMask or lockedMask or heldMask

    public fun onPointer(
        pointerId: Int,
        x: Float,
        y: Float,
        action: PointerAction,
        sink: InputSink,
    ) {
        if (pointerId !in 0 until MAX_POINTERS) return
        when (action) {
            PointerAction.DOWN -> press(pointerId, geometry.keyAt(x, y))
            PointerAction.MOVE -> Unit
            PointerAction.CANCEL -> {
                endHold(pointerKeys[pointerId])
                pointerKeys[pointerId] = NO_KEY
            }
            PointerAction.UP -> {
                val key = pointerKeys[pointerId]
                pointerKeys[pointerId] = NO_KEY
                if (key == NO_KEY) return
                val chorded = endHold(key)
                if (geometry.keyAt(x, y) == key) release(key, chorded, sink)
            }
        }
    }

    public fun isPressed(keyIndex: Int): Boolean = pointerKeys.any { it == keyIndex }

    public fun reset() {
        pointerKeys.fill(NO_KEY)
        oneShotMask = 0
        lockedMask = 0
        heldMask = 0
        chordedMask = 0
    }

    private fun press(pointerId: Int, key: Int) {
        pointerKeys[pointerId] = key
        val def = if (key == NO_KEY) null else geometry.spec(key).def
        if (def is KeyDef.Modifier) heldMask = heldMask or bit(def.kind)
    }

    private fun endHold(key: Int): Boolean {
        val def = if (key == NO_KEY) null else geometry.spec(key).def
        if (def !is KeyDef.Modifier) return false
        val bit = bit(def.kind)
        val chorded = chordedMask and bit != 0
        heldMask = heldMask and bit.inv()
        chordedMask = chordedMask and bit.inv()
        return chorded
    }

    private fun release(key: Int, chorded: Boolean, sink: InputSink) {
        when (val def = geometry.spec(key).def) {
            is KeyDef.Modifier -> if (!chorded) tapModifier(bit(def.kind))
            is KeyDef.Char -> {
                emitChar(def, sink)
                afterKey()
            }
            is KeyDef.Special -> {
                sink.sendKey(KeyCodes.forSpecial(def.code), metaState())
                afterKey()
            }
            is KeyDef.LayoutSwitch -> Unit
        }
    }

    private fun tapModifier(bit: Int) {
        when {
            lockedMask and bit != 0 -> lockedMask = lockedMask and bit.inv()
            oneShotMask and bit != 0 -> {
                oneShotMask = oneShotMask and bit.inv()
                lockedMask = lockedMask or bit
            }
            else -> oneShotMask = oneShotMask or bit
        }
    }

    private fun afterKey() {
        oneShotMask = 0
        chordedMask = chordedMask or heldMask
    }

    private fun emitChar(def: KeyDef.Char, sink: InputSink) {
        val shifted = isActive(ModifierKind.SHIFT)
        if (isActive(ModifierKind.CTRL) || isActive(ModifierKind.ALT)) {
            val keyCode = KeyCodes.forChar(def.primary[0])
            if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                sink.sendKey(keyCode, metaState())
                return
            }
        }
        sink.commitText(if (shifted) def.shifted else def.primary)
    }

    private fun metaState(): Int {
        var meta = 0
        if (isActive(ModifierKind.CTRL))
            meta = meta or KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        if (isActive(ModifierKind.ALT))
            meta = meta or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
        if (isActive(ModifierKind.SHIFT))
            meta = meta or KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
        return meta
    }

    private fun isActive(kind: ModifierKind): Boolean = modifierMask and bit(kind) != 0

    private fun bit(kind: ModifierKind): Int = 1 shl kind.ordinal

    private companion object {
        const val MAX_POINTERS = 10
        const val NO_KEY = -1
    }
}
