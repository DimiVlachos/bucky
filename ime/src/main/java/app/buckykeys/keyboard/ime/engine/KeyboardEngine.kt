package app.buckykeys.keyboard.ime.engine

import android.view.KeyEvent
import app.buckykeys.keyboard.ime.geometry.KeyboardGeometry
import app.buckykeys.keyboard.ime.layout.KeyCode
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.ModifierKind

public class KeyboardEngine(private val geometry: KeyboardGeometry) {

    private val pointerKeys = IntArray(MAX_POINTERS) { NO_KEY }
    private val pointerRepeated = BooleanArray(MAX_POINTERS)
    private var repeatPointer = NO_POINTER
    private var repeatAt = NO_DEADLINE

    private var oneShotMask = 0
    private var heldMask = 0
    private var chordedMask = 0

    public var lockedMask: Int = 0
        private set

    public val modifierMask: Int
        get() = oneShotMask or lockedMask or heldMask

    public val nextDeadline: Long
        get() = if (repeatPointer == NO_POINTER) NO_DEADLINE else repeatAt

    public fun onPointer(
        pointerId: Int,
        x: Float,
        y: Float,
        action: PointerAction,
        timeMs: Long,
        sink: InputSink,
    ) {
        if (pointerId !in 0 until MAX_POINTERS) return
        when (action) {
            PointerAction.DOWN -> press(pointerId, geometry.keyAt(x, y), timeMs)
            PointerAction.MOVE ->
                if (pointerId == repeatPointer && geometry.keyAt(x, y) != pointerKeys[pointerId]) {
                    stopRepeat()
                }
            PointerAction.CANCEL -> {
                if (pointerId == repeatPointer) stopRepeat()
                endHold(pointerKeys[pointerId])
                pointerKeys[pointerId] = NO_KEY
                pointerRepeated[pointerId] = false
            }
            PointerAction.UP -> lift(pointerId, x, y, sink)
        }
    }

    public fun onTick(timeMs: Long, sink: InputSink) {
        if (repeatPointer == NO_POINTER || timeMs < repeatAt) return
        when (val def = geometry.spec(pointerKeys[repeatPointer]).def) {
            is KeyDef.Special -> sink.sendKey(KeyCodes.forSpecial(def.code), metaState())
            is KeyDef.Char -> emitChar(def, sink)
            is KeyDef.Modifier,
            is KeyDef.LayoutSwitch -> Unit
        }
        pointerRepeated[repeatPointer] = true
        chordedMask = chordedMask or heldMask
        repeatAt = timeMs + REPEAT_INTERVAL_MS
    }

    public fun isPressed(keyIndex: Int): Boolean = pointerKeys.any { it == keyIndex }

    public fun reset() {
        pointerKeys.fill(NO_KEY)
        pointerRepeated.fill(false)
        repeatPointer = NO_POINTER
        oneShotMask = 0
        lockedMask = 0
        heldMask = 0
        chordedMask = 0
    }

    private fun press(pointerId: Int, key: Int, timeMs: Long) {
        val def = if (key == NO_KEY) null else geometry.spec(key).def
        if (def !is KeyDef.Modifier) stopRepeat()
        pointerKeys[pointerId] = key
        pointerRepeated[pointerId] = false
        if (def is KeyDef.Modifier) heldMask = heldMask or bit(def.kind)
        if (def != null && repeats(def)) {
            repeatPointer = pointerId
            repeatAt = timeMs + REPEAT_DELAY_MS
        }
    }

    private fun lift(pointerId: Int, x: Float, y: Float, sink: InputSink) {
        if (pointerId == repeatPointer) stopRepeat()
        val key = pointerKeys[pointerId]
        val repeated = pointerRepeated[pointerId]
        pointerKeys[pointerId] = NO_KEY
        pointerRepeated[pointerId] = false
        if (key == NO_KEY) return
        val chorded = endHold(key)
        if (!repeated && geometry.keyAt(x, y) == key) release(key, chorded, sink)
    }

    private fun stopRepeat() {
        if (repeatPointer == NO_POINTER) return
        if (pointerRepeated[repeatPointer]) oneShotMask = 0
        repeatPointer = NO_POINTER
    }

    private fun repeats(def: KeyDef): Boolean =
        when (def) {
            is KeyDef.Special ->
                when (def.code) {
                    KeyCode.BACKSPACE,
                    KeyCode.FORWARD_DEL,
                    KeyCode.UP,
                    KeyCode.DOWN,
                    KeyCode.LEFT,
                    KeyCode.RIGHT -> true
                    else -> false
                }
            is KeyDef.Char -> def.primary == " "
            is KeyDef.Modifier,
            is KeyDef.LayoutSwitch -> false
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

    public companion object {
        public const val NO_DEADLINE: Long = -1L
        private const val MAX_POINTERS = 10
        private const val NO_KEY = -1
        private const val NO_POINTER = -1
        private const val REPEAT_DELAY_MS = 400L
        private const val REPEAT_INTERVAL_MS = 50L
    }
}
