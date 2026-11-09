package app.buckykeys.keyboard.ime.engine

import android.view.KeyEvent
import app.buckykeys.keyboard.ime.geometry.KeyboardGeometry
import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.KeyCode
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.ModifierKind
import app.buckykeys.keyboard.ime.layout.longPressAlternates
import kotlin.math.abs
import kotlin.math.min

internal class KeyboardEngine(
    geometry: KeyboardGeometry,
    private val longPressMs: Long,
    private val bias: TouchBias = TouchBias(),
) {

    private var geometry = geometry

    private val pointerKeys = IntArray(MAX_POINTERS) { NO_KEY }
    private val pointerConsumed = BooleanArray(MAX_POINTERS)
    private val pointerDownX = FloatArray(MAX_POINTERS)
    private val pointerStartKey = IntArray(MAX_POINTERS) { NO_KEY }
    private val pointerStartX = FloatArray(MAX_POINTERS)
    private val pointerStartY = FloatArray(MAX_POINTERS)
    private val pointerFn = BooleanArray(MAX_POINTERS)
    private val pointerX = FloatArray(MAX_POINTERS)
    private val pointerY = FloatArray(MAX_POINTERS)
    private val keptKeys = IntArray(MAX_POINTERS)
    private val keptHolds = BooleanArray(MAX_POINTERS)
    private var timerPointer = NO_POINTER
    private var timerAt = NO_DEADLINE
    private var timerRepeats = false
    private var popupPointer = NO_POINTER
    private var previewPointer = NO_POINTER
    private var deadMark: String? = null
    private var deadKey = NO_DEAD_KEY
    private var switchRequest: String? = null

    private var oneShotMask = 0
    private var heldMask = 0
    private var chordedMask = 0
    private val heldCounts = IntArray(MODIFIER_KINDS)

    public val popup: AlternatesPopup = AlternatesPopup()

    public var languageSwipe: Boolean = false

    public var fnToggles: Boolean = false

    public var autoShift: Boolean = false
        private set

    public fun setAutoShift(on: Boolean) {
        autoShift = on
    }

    public var lockedMask: Int = 0
        private set

    public val modifierMask: Int
        get() = oneShotMask or lockedMask or heldMask or autoMask

    private val autoMask: Int
        get() = if (autoShift) bit(ModifierKind.SHIFT) else 0

    public val nextDeadline: Long
        get() = if (timerPointer == NO_POINTER) NO_DEADLINE else timerAt

    public val previewKey: Int
        get() = if (previewPointer == NO_POINTER) NO_PREVIEW else pointerKeys[previewPointer]

    public val pendingDeadKey: Int
        get() = deadKey

    public val deadKeyShifted: Boolean
        get() = (oneShotMask or heldMask) and bit(ModifierKind.SHIFT) != 0

    public var longPressCount: Int = 0
        private set

    public fun setGeometry(geometry: KeyboardGeometry, keepPointers: Boolean = false) {
        val previous = this.geometry
        for (p in 0 until MAX_POINTERS) {
            val key = pointerKeys[p]
            val target =
                if (keepPointers && key != NO_KEY && !pointerConsumed[p]) {
                    keyAt(geometry, pointerX[p], pointerY[p])
                } else {
                    NO_KEY
                }
            val def = if (key == NO_KEY) null else previous.spec(key).def
            keptHolds[p] =
                def is KeyDef.Modifier && target != NO_KEY && geometry.spec(target).def == def
            keptKeys[p] = if (def is KeyDef.Modifier && !keptHolds[p]) NO_KEY else target
        }
        val chorded = chordedMask
        this.geometry = geometry
        releaseAllPointers()
        for (p in 0 until MAX_POINTERS) {
            pointerKeys[p] = keptKeys[p]
            if (!keptHolds[p]) continue
            val held = geometry.spec(keptKeys[p]).def
            if (held is KeyDef.Modifier) startHold(held.kind)
        }
        chordedMask = chorded and heldMask
        deadKey = findDeadKey(deadMark)
        if (deadKey == NO_DEAD_KEY) deadMark = null
    }

    public fun takeSwitchRequest(): String? {
        val request = switchRequest
        switchRequest = null
        return request
    }

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
            PointerAction.DOWN -> press(pointerId, keyAt(geometry, x, y), x, y, timeMs, sink)
            PointerAction.MOVE -> move(pointerId, x, y, timeMs)
            PointerAction.CANCEL -> cancel(pointerId)
            PointerAction.UP -> lift(pointerId, x, y, sink)
        }
    }

    public fun onTick(timeMs: Long, sink: InputSink) {
        if (timerPointer == NO_POINTER || timeMs < timerAt) return
        val pointerId = timerPointer
        val def = acting(pointerKeys[pointerId], pointerFn[pointerId])
        if (timerRepeats) {
            emit(def, sink)
            pointerConsumed[pointerId] = true
            chordedMask = chordedMask or heldMask
            timerAt = timeMs + REPEAT_INTERVAL_MS
        } else {
            timerPointer = NO_POINTER
            when (def) {
                is KeyDef.Char,
                is KeyDef.Dead -> longPress(pointerId, def, sink)
                is KeyDef.LayoutSwitch -> holdSwitch(pointerId, def)
                else -> Unit
            }
        }
    }

    public fun isPressed(keyIndex: Int): Boolean = pointerKeys.any { it == keyIndex }

    public fun reset() {
        releaseAllPointers()
        clearDead()
        switchRequest = null
        oneShotMask = 0
        lockedMask = 0
        autoShift = false
    }

    private fun releaseAllPointers() {
        pointerKeys.fill(NO_KEY)
        pointerConsumed.fill(false)
        pointerFn.fill(false)
        timerPointer = NO_POINTER
        previewPointer = NO_POINTER
        closePopup()
        heldCounts.fill(0)
        heldMask = 0
        chordedMask = 0
    }

    private fun findDeadKey(mark: String?): Int {
        if (mark == null) return NO_DEAD_KEY
        for (i in 0 until geometry.keyCount) {
            val def = geometry.spec(i).def
            if (def is KeyDef.Dead && (def.primary == mark || def.shifted == mark)) return i
        }
        return NO_DEAD_KEY
    }

    private fun press(pointerId: Int, key: Int, x: Float, y: Float, timeMs: Long, sink: InputSink) {
        val def = if (key == NO_KEY) null else geometry.spec(key).def
        if (def !is KeyDef.Modifier) {
            stopTimer()
            closePopup()
        }
        if (def != null && def !is KeyDef.Modifier) {
            for (other in 0 until MAX_POINTERS) if (other != pointerId) commitRolled(other, sink)
        }
        endHold(pointerKeys[pointerId])
        pointerKeys[pointerId] = key
        pointerConsumed[pointerId] = false
        pointerDownX[pointerId] = x
        pointerStartKey[pointerId] = key
        pointerStartX[pointerId] = x
        pointerStartY[pointerId] = y
        pointerX[pointerId] = x
        pointerY[pointerId] = y
        if (def is KeyDef.Modifier) startHold(def.kind)
        aim(pointerId, key, timeMs)
    }

    private fun aim(pointerId: Int, key: Int, timeMs: Long) {
        pointerFn[pointerId] =
            key != NO_KEY && geometry.spec(key).fn != null && isActive(ModifierKind.FN)
        val acting = if (key == NO_KEY) null else acting(key, pointerFn[pointerId])
        when (acting) {
            is KeyDef.Modifier -> Unit
            null -> if (pointerId == previewPointer) previewPointer = NO_POINTER
            else -> previewPointer = pointerId
        }
        when {
            acting == null -> Unit
            repeats(acting) -> startTimer(pointerId, timeMs + REPEAT_DELAY_MS, repeats = true)
            longPresses(acting) -> startTimer(pointerId, timeMs + longPressMs, repeats = false)
        }
    }

    // Fast typists press the next key before lifting the last one; typing the held key now keeps
    // the order, as Hacker's Keyboard does.
    private fun commitRolled(pointerId: Int, sink: InputSink) {
        val key = pointerKeys[pointerId]
        if (key == NO_KEY || pointerConsumed[pointerId] || pointerId == popupPointer) return
        val spec = geometry.spec(key)
        val def = acting(key, pointerFn[pointerId])
        if (def is KeyDef.Modifier || def is KeyDef.LayoutSwitch || spec.then != null) return
        if (pointerId == previewPointer) previewPointer = NO_POINTER
        pointerConsumed[pointerId] = true
        learnFrom(pointerId, key)
        release(key, false, pointerFn[pointerId], sink)
    }

    // Only plain taps on character keys teach the offset; slides, long presses and wide keys say
    // little about where this person's finger lands.
    private fun learnFrom(pointerId: Int, key: Int) {
        if (key != pointerStartKey[pointerId] || geometry.spec(key).def !is KeyDef.Char) return
        val r = geometry.rects
        val i = key * 4
        val row = geometry.rowHeightPx.toFloat()
        if (row <= 0f || r[i + 2] - r[i] > row * MAX_LEARNING_WIDTH_ROWS) return
        bias.learn(
            (pointerStartX[pointerId] - (r[i] + r[i + 2]) / 2f) / row,
            (pointerStartY[pointerId] - (r[i + 1] + r[i + 3]) / 2f) / row,
        )
    }

    private fun keyAt(geometry: KeyboardGeometry, x: Float, y: Float): Int {
        val row = geometry.rowHeightPx
        return geometry.keyAt(x, y, bias.x * row, bias.y * row)
    }

    private fun move(pointerId: Int, x: Float, y: Float, timeMs: Long) {
        pointerX[pointerId] = x
        pointerY[pointerId] = y
        if (isLanguageSwipe(pointerId, x)) {
            swipeLanguage(pointerId, forward = x > pointerDownX[pointerId])
            return
        }
        if (pointerId == popupPointer) {
            popup.select(x, y)
            return
        }
        val key = pointerKeys[pointerId]
        if (staysOn(key, x, y)) return
        if (pointerConsumed[pointerId] || (key != NO_KEY && isModifier(key))) {
            if (pointerId == previewPointer) previewPointer = NO_POINTER
            if (pointerId == timerPointer) stopTimer()
            return
        }
        val target = keyAt(geometry, x, y)
        slideTo(
            pointerId,
            if (target == NO_KEY || isModifier(target)) NO_KEY else target,
            x,
            timeMs,
        )
    }

    private fun slideTo(pointerId: Int, key: Int, x: Float, timeMs: Long) {
        if (key == pointerKeys[pointerId]) return
        if (pointerId == timerPointer) stopTimer()
        pointerKeys[pointerId] = key
        pointerDownX[pointerId] = x
        aim(pointerId, key, timeMs)
    }

    private fun isLanguageSwipe(pointerId: Int, x: Float): Boolean {
        if (!languageSwipe || pointerConsumed[pointerId]) return false
        val key = pointerKeys[pointerId]
        if (key == NO_KEY || !isSpace(geometry.spec(key).def)) return false
        return abs(x - pointerDownX[pointerId]) >= geometry.widthPx * LANGUAGE_SWIPE_FRACTION
    }

    private fun swipeLanguage(pointerId: Int, forward: Boolean) {
        if (pointerId == timerPointer) stopTimer()
        if (pointerId == previewPointer) previewPointer = NO_POINTER
        pointerConsumed[pointerId] = true
        switchRequest =
            if (forward) BuiltInLayouts.NEXT_LANGUAGE else BuiltInLayouts.PREVIOUS_LANGUAGE
    }

    private fun isSpace(def: KeyDef): Boolean = def is KeyDef.Char && def.primary == " "

    private fun cancel(pointerId: Int) {
        if (pointerId == previewPointer) previewPointer = NO_POINTER
        if (pointerId == timerPointer) stopTimer()
        if (pointerId == popupPointer) closePopup()
        endHold(pointerKeys[pointerId])
        pointerKeys[pointerId] = NO_KEY
        pointerConsumed[pointerId] = false
        pointerFn[pointerId] = false
    }

    private fun lift(pointerId: Int, x: Float, y: Float, sink: InputSink) {
        if (pointerId == previewPointer) previewPointer = NO_POINTER
        if (pointerId == timerPointer) stopTimer()
        val key = pointerKeys[pointerId]
        val consumed = pointerConsumed[pointerId]
        val usesFn = pointerFn[pointerId]
        pointerKeys[pointerId] = NO_KEY
        pointerConsumed[pointerId] = false
        pointerFn[pointerId] = false
        if (pointerId == popupPointer) {
            popup.select(x, y)
            commitPopup(key, sink)
            return
        }
        if (key == NO_KEY) return
        val chorded = endHold(key)
        if (consumed) return
        if (staysOn(key, x, y)) {
            learnFrom(pointerId, key)
            release(key, chorded, usesFn, sink)
            return
        }
        val target = keyAt(geometry, x, y)
        if (target == NO_KEY || isModifier(key) || isModifier(target)) return
        release(target, false, geometry.spec(target).fn != null && isActive(ModifierKind.FN), sink)
    }

    private fun isModifier(key: Int): Boolean = geometry.spec(key).def is KeyDef.Modifier

    private fun staysOn(key: Int, x: Float, y: Float): Boolean {
        if (key == NO_KEY) return false
        if (keyAt(geometry, x, y) == key) return true
        val r = geometry.rects
        val i = key * 4
        val slop = min(r[i + 2] - r[i], r[i + 3] - r[i + 1]) * SLIP_FRACTION
        val cx = x - bias.x * geometry.rowHeightPx
        val cy = y - bias.y * geometry.rowHeightPx
        return cx > r[i] - slop &&
            cx < r[i + 2] + slop &&
            cy > r[i + 1] - slop &&
            cy < r[i + 3] + slop
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

    private fun longPress(pointerId: Int, def: KeyDef, sink: InputSink) {
        pointerConsumed[pointerId] = true
        longPressCount++
        if (pointerId == previewPointer) previewPointer = NO_POINTER
        val alternates = def.longPressAlternates
        if (alternates.isEmpty()) {
            if (def is KeyDef.Char) emitChar(def, sink, forceShift = true)
            afterKey()
        } else {
            popup.open(
                pointerKeys[pointerId],
                alternates.size,
                geometry.rects,
                geometry.widthPx.toFloat(),
            )
            popupPointer = pointerId
        }
    }

    private fun holdSwitch(pointerId: Int, def: KeyDef.LayoutSwitch) {
        val target = def.holdTargetId ?: return
        pointerConsumed[pointerId] = true
        longPressCount++
        switchRequest = target
    }

    private fun commitPopup(key: Int, sink: InputSink) {
        val selected = popup.selected
        closePopup()
        val alternates =
            if (key == NO_KEY) emptyList() else geometry.spec(key).def.longPressAlternates
        if (selected == AlternatesPopup.NONE || selected !in alternates.indices) return
        emit(alternates[selected], sink)
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
                    KeyCode.RIGHT,
                    KeyCode.PAGE_UP,
                    KeyCode.PAGE_DOWN -> true
                    else -> false
                }
            is KeyDef.Char,
            is KeyDef.Dead,
            is KeyDef.Modifier,
            is KeyDef.LayoutSwitch -> false
        }

    private fun longPresses(def: KeyDef): Boolean =
        when (def) {
            is KeyDef.Char -> def.alternates.isNotEmpty() || def.shifted != def.primary
            is KeyDef.Dead -> def.alternates.isNotEmpty()
            is KeyDef.LayoutSwitch -> def.holdTargetId != null
            else -> false
        }

    private fun startHold(kind: ModifierKind) {
        val bit = bit(kind)
        if (heldCounts[kind.ordinal] > 0) chordedMask = chordedMask or bit
        heldCounts[kind.ordinal]++
        heldMask = heldMask or bit
    }

    private fun endHold(key: Int): Boolean {
        val def = if (key == NO_KEY) null else geometry.spec(key).def
        if (def !is KeyDef.Modifier) return false
        val kind = def.kind.ordinal
        if (heldCounts[kind] == 0) return false
        heldCounts[kind]--
        if (heldCounts[kind] > 0) return true
        val bit = bit(def.kind)
        val chorded = chordedMask and bit != 0
        heldMask = heldMask and bit.inv()
        chordedMask = chordedMask and bit.inv()
        return chorded
    }

    private fun acting(key: Int, usesFn: Boolean): KeyDef {
        val spec = geometry.spec(key)
        return if (usesFn) spec.fn ?: spec.def else spec.def
    }

    private fun release(key: Int, chorded: Boolean, usesFn: Boolean, sink: InputSink) {
        when (val def = acting(key, usesFn)) {
            is KeyDef.Modifier -> if (!chorded) tapModifier(def.kind)
            is KeyDef.LayoutSwitch -> switchRequest = def.targetId
            is KeyDef.Dead -> {
                val auto = autoShift
                pressDead(key, def, sink)
                afterKey()
                autoShift = auto
            }
            is KeyDef.Char,
            is KeyDef.Special -> {
                emit(def, sink)
                afterKey()
                geometry.spec(key).then?.let { switchRequest = it }
            }
        }
    }

    private fun tapModifier(kind: ModifierKind) {
        val bit = bit(kind)
        when {
            kind == ModifierKind.SHIFT && autoShift -> autoShift = false
            lockedMask and bit != 0 -> lockedMask = lockedMask and bit.inv()
            kind == ModifierKind.FN && fnToggles -> {
                oneShotMask = oneShotMask and bit.inv()
                lockedMask = lockedMask or bit
            }
            oneShotMask and bit != 0 -> {
                oneShotMask = oneShotMask and bit.inv()
                lockedMask = lockedMask or bit
            }
            else -> oneShotMask = oneShotMask or bit
        }
    }

    private fun afterKey() {
        oneShotMask = 0
        autoShift = false
        chordedMask = chordedMask or heldMask
    }

    private fun emit(def: KeyDef, sink: InputSink) {
        when (def) {
            is KeyDef.Special -> emitSpecial(def, sink)
            is KeyDef.Char -> emitChar(def, sink, forceShift = false)
            is KeyDef.Dead,
            is KeyDef.Modifier,
            is KeyDef.LayoutSwitch -> Unit
        }
    }

    private fun emitSpecial(def: KeyDef.Special, sink: InputSink) {
        if (deadMark != null) {
            clearDead()
            if (def.code == KeyCode.BACKSPACE) return
        }
        sink.sendKey(KeyCodes.forSpecial(def.code), metaState(forceShift = false))
    }

    private fun pressDead(key: Int, def: KeyDef.Dead, sink: InputSink) {
        val latin = def.latin
        if (!latin.isNullOrEmpty() && sendsKeyChord()) {
            val keyCode = KeyCodes.forChar(latin[0])
            if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                clearDead()
                sink.sendKey(keyCode, metaState(forceShift = false))
                return
            }
        }
        val mark = if (deadKeyShifted) def.shifted else def.primary
        val pending = deadMark
        if (pending != null) {
            sink.commitText(pending)
            clearDead()
            if (pending == mark) return
        }
        deadMark = mark
        deadKey = key
    }

    private fun clearDead() {
        deadMark = null
        deadKey = NO_DEAD_KEY
    }

    private fun emitChar(def: KeyDef.Char, sink: InputSink, forceShift: Boolean) {
        val shifted = forceShift || userShift() || (autoShift && isLetter(def.primary))
        if (sendsKeyChord()) {
            val keyCode = KeyCodes.forChar((def.latin ?: def.primary)[0])
            if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                clearDead()
                sink.sendKey(keyCode, metaState(forceShift))
                return
            }
        }
        val pending = deadMark
        if (pending != null) {
            clearDead()
            val composed = (if (shifted) def.composedShifted else def.composed)[pending]
            if (composed != null) {
                sink.commitText(composed)
                return
            }
            sink.commitText(pending)
            if (def.primary == " ") return
        }
        sink.commitText(if (shifted) def.shifted else def.primary)
    }

    private fun metaState(forceShift: Boolean): Int {
        var meta = 0
        if (isActive(ModifierKind.CTRL))
            meta = meta or KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        if (isActive(ModifierKind.ALT))
            meta = meta or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
        if (isActive(ModifierKind.META))
            meta = meta or KeyEvent.META_META_ON or KeyEvent.META_META_LEFT_ON
        if (forceShift || userShift())
            meta = meta or KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
        return meta
    }

    private fun isActive(kind: ModifierKind): Boolean = modifierMask and bit(kind) != 0

    private fun sendsKeyChord(): Boolean =
        isActive(ModifierKind.CTRL) || isActive(ModifierKind.ALT) || isActive(ModifierKind.META)

    private fun isLetter(text: String): Boolean =
        text.isNotEmpty() && Character.isLetter(text.codePointAt(0))

    // Auto shift only capitalises letters; key events carry the user's own Shift.
    private fun userShift(): Boolean =
        (oneShotMask or lockedMask or heldMask) and bit(ModifierKind.SHIFT) != 0

    private fun bit(kind: ModifierKind): Int = 1 shl kind.ordinal

    public companion object {
        public const val NO_DEADLINE: Long = -1L
        public const val NO_PREVIEW: Int = -1
        public const val NO_DEAD_KEY: Int = -1
        private const val MAX_POINTERS = 10
        private val MODIFIER_KINDS = ModifierKind.entries.size
        private const val NO_KEY = -1
        private const val NO_POINTER = -1
        private const val REPEAT_DELAY_MS = 400L
        private const val REPEAT_INTERVAL_MS = 50L
        private const val LANGUAGE_SWIPE_FRACTION = 0.08f
        private const val SLIP_FRACTION = 0.4f
        private const val MAX_LEARNING_WIDTH_ROWS = 1.5f
    }
}
