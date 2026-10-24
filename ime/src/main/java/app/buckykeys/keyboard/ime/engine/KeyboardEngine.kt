package app.buckykeys.keyboard.ime.engine

import android.view.KeyEvent
import app.buckykeys.keyboard.ime.geometry.KeyboardGeometry
import app.buckykeys.keyboard.ime.layout.KeyCode
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.ModifierKind

public class KeyboardEngine(private val geometry: KeyboardGeometry) {

    private val pointerKeys = IntArray(MAX_POINTERS) { NO_KEY }
    private val pointerConsumed = BooleanArray(MAX_POINTERS)
    private var timerPointer = NO_POINTER
    private var timerAt = NO_DEADLINE
    private var timerRepeats = false
    private var popupPointer = NO_POINTER

    private var oneShotMask = 0
    private var heldMask = 0
    private var chordedMask = 0

    public val popup: AlternatesPopup = AlternatesPopup()

    public var lockedMask: Int = 0
        private set

    public val modifierMask: Int
        get() = oneShotMask or lockedMask or heldMask

    public val nextDeadline: Long
        get() = if (timerPointer == NO_POINTER) NO_DEADLINE else timerAt

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
            PointerAction.MOVE -> move(pointerId, x, y)
            PointerAction.CANCEL -> cancel(pointerId)
            PointerAction.UP -> lift(pointerId, x, y, sink)
        }
    }

    public fun onTick(timeMs: Long, sink: InputSink) {
        if (timerPointer == NO_POINTER || timeMs < timerAt) return
        val pointerId = timerPointer
        val def = geometry.spec(pointerKeys[pointerId]).def
        if (timerRepeats) {
            emit(def, sink)
            pointerConsumed[pointerId] = true
            chordedMask = chordedMask or heldMask
            timerAt = timeMs + REPEAT_INTERVAL_MS
        } else {
            timerPointer = NO_POINTER
            if (def is KeyDef.Char) longPress(pointerId, def, sink)
        }
    }

    public fun isPressed(keyIndex: Int): Boolean = pointerKeys.any { it == keyIndex }

    public fun reset() {
        pointerKeys.fill(NO_KEY)
        pointerConsumed.fill(false)
        timerPointer = NO_POINTER
        closePopup()
        oneShotMask = 0
        lockedMask = 0
        heldMask = 0
        chordedMask = 0
    }

    private fun press(pointerId: Int, key: Int, timeMs: Long) {
        val def = if (key == NO_KEY) null else geometry.spec(key).def
        if (def !is KeyDef.Modifier) {
            stopTimer()
            closePopup()
        }
        pointerKeys[pointerId] = key
        pointerConsumed[pointerId] = false
        if (def is KeyDef.Modifier) heldMask = heldMask or bit(def.kind)
        when {
            def == null -> Unit
            repeats(def) -> startTimer(pointerId, timeMs + REPEAT_DELAY_MS, repeats = true)
            longPresses(def) -> startTimer(pointerId, timeMs + LONG_PRESS_MS, repeats = false)
        }
    }

    private fun move(pointerId: Int, x: Float, y: Float) {
        when (pointerId) {
            popupPointer -> popup.select(x, y)
            timerPointer -> if (geometry.keyAt(x, y) != pointerKeys[pointerId]) stopTimer()
        }
    }

    private fun cancel(pointerId: Int) {
        if (pointerId == timerPointer) stopTimer()
        if (pointerId == popupPointer) closePopup()
        endHold(pointerKeys[pointerId])
        pointerKeys[pointerId] = NO_KEY
        pointerConsumed[pointerId] = false
    }

    private fun lift(pointerId: Int, x: Float, y: Float, sink: InputSink) {
        if (pointerId == timerPointer) stopTimer()
        val key = pointerKeys[pointerId]
        val consumed = pointerConsumed[pointerId]
        pointerKeys[pointerId] = NO_KEY
        pointerConsumed[pointerId] = false
        if (pointerId == popupPointer) {
            commitPopup(key, sink)
            return
        }
        if (key == NO_KEY) return
        val chorded = endHold(key)
        if (!consumed && geometry.keyAt(x, y) == key) release(key, chorded, sink)
    }

    private fun startTimer(pointerId: Int, at: Long, repeats: Boolean) {
        timerPointer = pointerId
        timerAt = at
        timerRepeats = repeats
    }

    private fun stopTimer() {
        if (timerPointer == NO_POINTER) return
        if (timerRepeats && pointerConsumed[timerPointer]) oneShotMask = 0
        timerPointer = NO_POINTER
    }

    private fun longPress(pointerId: Int, def: KeyDef.Char, sink: InputSink) {
        pointerConsumed[pointerId] = true
        if (def.alternates.isEmpty()) {
            emitChar(def, sink, forceShift = true)
            afterKey()
        } else {
            popup.open(
                pointerKeys[pointerId],
                def.alternates.size,
                geometry.rects,
                geometry.widthPx.toFloat(),
            )
            popupPointer = pointerId
        }
    }

    private fun commitPopup(key: Int, sink: InputSink) {
        val selected = popup.selected
        closePopup()
        val def = if (key == NO_KEY) null else geometry.spec(key).def
        if (selected == AlternatesPopup.NONE || def !is KeyDef.Char) return
        emit(def.alternates[selected], sink)
        afterKey()
    }

    private fun closePopup() {
        popup.close()
        popupPointer = NO_POINTER
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

    private fun longPresses(def: KeyDef): Boolean =
        def is KeyDef.Char && (def.alternates.isNotEmpty() || def.shifted != def.primary)

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
            is KeyDef.LayoutSwitch -> Unit
            is KeyDef.Char,
            is KeyDef.Special -> {
                emit(def, sink)
                afterKey()
            }
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

    private fun emit(def: KeyDef, sink: InputSink) {
        when (def) {
            is KeyDef.Special ->
                sink.sendKey(KeyCodes.forSpecial(def.code), metaState(forceShift = false))
            is KeyDef.Char -> emitChar(def, sink, forceShift = false)
            is KeyDef.Modifier,
            is KeyDef.LayoutSwitch -> Unit
        }
    }

    private fun emitChar(def: KeyDef.Char, sink: InputSink, forceShift: Boolean) {
        val shifted = forceShift || isActive(ModifierKind.SHIFT)
        if (isActive(ModifierKind.CTRL) || isActive(ModifierKind.ALT)) {
            val keyCode = KeyCodes.forChar(def.primary[0])
            if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                sink.sendKey(keyCode, metaState(forceShift))
                return
            }
        }
        sink.commitText(if (shifted) def.shifted else def.primary)
    }

    private fun metaState(forceShift: Boolean): Int {
        var meta = 0
        if (isActive(ModifierKind.CTRL))
            meta = meta or KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        if (isActive(ModifierKind.ALT))
            meta = meta or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
        if (forceShift || isActive(ModifierKind.SHIFT))
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
        private const val LONG_PRESS_MS = 300L
        private const val REPEAT_DELAY_MS = 400L
        private const val REPEAT_INTERVAL_MS = 50L
    }
}
