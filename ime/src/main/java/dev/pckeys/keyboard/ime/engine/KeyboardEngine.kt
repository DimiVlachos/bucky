package dev.pckeys.keyboard.ime.engine

import android.view.KeyEvent
import dev.pckeys.keyboard.ime.geometry.KeyboardGeometry
import dev.pckeys.keyboard.ime.layout.KeyDef
import dev.pckeys.keyboard.ime.layout.ModifierKind

public class KeyboardEngine(private val geometry: KeyboardGeometry) {

    private val pointerKeys = IntArray(MAX_POINTERS) { NO_KEY }

    public var modifierMask: Int = 0
        private set

    public fun onPointer(
        pointerId: Int,
        x: Float,
        y: Float,
        action: PointerAction,
        sink: InputSink,
    ) {
        if (pointerId !in 0 until MAX_POINTERS) return
        when (action) {
            PointerAction.DOWN -> pointerKeys[pointerId] = geometry.keyAt(x, y)
            PointerAction.MOVE -> Unit
            PointerAction.CANCEL -> pointerKeys[pointerId] = NO_KEY
            PointerAction.UP -> {
                val key = pointerKeys[pointerId]
                pointerKeys[pointerId] = NO_KEY
                if (key != NO_KEY && geometry.keyAt(x, y) == key) release(key, sink)
            }
        }
    }

    public fun isPressed(keyIndex: Int): Boolean = pointerKeys.any { it == keyIndex }

    public fun reset() {
        pointerKeys.fill(NO_KEY)
        modifierMask = 0
    }

    private fun release(key: Int, sink: InputSink) {
        when (val def = geometry.spec(key).def) {
            is KeyDef.Modifier -> modifierMask = modifierMask xor bit(def.kind)
            is KeyDef.Char -> {
                emitChar(def, sink)
                modifierMask = 0
            }
            is KeyDef.Special -> {
                sink.sendKey(KeyCodes.forSpecial(def.code), metaState())
                modifierMask = 0
            }
            is KeyDef.LayoutSwitch -> Unit
        }
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
