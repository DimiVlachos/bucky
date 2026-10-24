package app.buckykeys.keyboard.ime.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.buckykeys.keyboard.ime.R
import app.buckykeys.keyboard.ime.engine.InputSink
import app.buckykeys.keyboard.ime.engine.KeyboardEngine
import app.buckykeys.keyboard.ime.engine.PointerAction
import app.buckykeys.keyboard.ime.geometry.KeyboardGeometry
import app.buckykeys.keyboard.ime.layout.DefaultLabels
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.ime.layout.ModifierKind
import timber.log.Timber

@SuppressLint("ViewConstructor")
public class KeyboardView(
    context: Context,
    private val layout: Layout,
    private val sink: InputSink,
    private val latencyLogging: Boolean,
) : View(context) {

    private val rowHeightPx = resources.getDimensionPixelSize(R.dimen.key_row_height)
    private val gapPx = resources.getDimension(R.dimen.key_gap)
    private val cornerPx = resources.getDimension(R.dimen.key_corner_radius)
    private val labelPaddingPx = resources.getDimension(R.dimen.key_label_padding)
    private val labelMinSizePx = resources.getDimension(R.dimen.key_label_min_size)
    private val labelSizePx = resources.getDimension(R.dimen.key_label_size)
    private val smallLabelSizePx = resources.getDimension(R.dimen.key_label_small_size)

    public val keysTop: Int = rowHeightPx

    private val backgroundPaint = fillPaint(R.color.keyboard_background)

    private val keyPaint = fillPaint(R.color.key_background)
    private val pressedPaint = fillPaint(R.color.key_pressed)
    private val activePaint = fillPaint(R.color.key_modifier_active)
    private val lockedPaint = fillPaint(R.color.key_modifier_locked)
    private val labelPaint = labelPaint(R.dimen.key_label_size)
    private val smallLabelPaint = labelPaint(R.dimen.key_label_small_size)

    private var geometry = KeyboardGeometry(layout, 0, rowHeightPx)
    private var engine = KeyboardEngine(geometry)
    private var labelX = FloatArray(0)
    private var labelY = FloatArray(0)
    private var labelSize = FloatArray(0)
    private var modifierBits = IntArray(0)
    private var pendingDownTime = NO_PENDING
    private var scheduledDeadline = KeyboardEngine.NO_DEADLINE
    private val timerTick = Runnable { onTimer() }

    init {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val bottom =
                insets
                    .getInsets(
                        WindowInsetsCompat.Type.navigationBars() or
                            WindowInsetsCompat.Type.tappableElement(),
                    )
                    .bottom
            view.setPadding(0, 0, 0, bottom)
            insets
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            keysTop + layout.rows.size * rowHeightPx + paddingBottom,
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        geometry = KeyboardGeometry(layout, w, rowHeightPx)
        engine = KeyboardEngine(geometry)
        scheduleTimer()
        val count = geometry.keyCount
        labelX = FloatArray(count)
        labelY = FloatArray(count)
        labelSize = FloatArray(count)
        modifierBits = IntArray(count)
        val rects = geometry.rects
        for (i in 0 until count) {
            val label = geometry.spec(i).label
            val paint = paintFor(label)
            paint.textSize = baseSizeFor(label)
            labelSize[i] =
                fittedTextSize(
                    paint.textSize,
                    paint.measureText(label),
                    rects[i * 4 + 2] - rects[i * 4] - 2 * (gapPx + labelPaddingPx),
                    labelMinSizePx,
                )
            paint.textSize = labelSize[i]
            labelX[i] = (rects[i * 4] + rects[i * 4 + 2]) / 2f
            labelY[i] =
                (rects[i * 4 + 1] + rects[i * 4 + 3]) / 2f - (paint.ascent() + paint.descent()) / 2f
            val def = geometry.spec(i).def
            modifierBits[i] = if (def is KeyDef.Modifier) 1 shl def.kind.ordinal else 0
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, keysTop.toFloat(), width.toFloat(), height.toFloat(), backgroundPaint)
        canvas.save()
        canvas.translate(0f, keysTop.toFloat())
        val rects = geometry.rects
        val mask = engine.modifierMask
        val locked = engine.lockedMask
        val shifted = mask and (1 shl ModifierKind.SHIFT.ordinal) != 0
        for (i in 0 until geometry.keyCount) {
            val fill =
                when {
                    engine.isPressed(i) -> pressedPaint
                    modifierBits[i] and locked != 0 -> lockedPaint
                    modifierBits[i] and mask != 0 -> activePaint
                    else -> keyPaint
                }
            canvas.drawRoundRect(
                rects[i * 4] + gapPx,
                rects[i * 4 + 1] + gapPx,
                rects[i * 4 + 2] - gapPx,
                rects[i * 4 + 3] - gapPx,
                cornerPx,
                cornerPx,
                fill,
            )
            val spec = geometry.spec(i)
            val def = spec.def
            val label = if (shifted && def is KeyDef.Char) def.shifted else spec.label
            val paint = paintFor(spec.label)
            paint.textSize = labelSize[i]
            canvas.drawText(label, labelX[i], labelY[i], paint)
        }
        drawPopup(canvas, shifted)
        canvas.restore()
        if (pendingDownTime != NO_PENDING) {
            Timber.tag(LATENCY_TAG)
                .d("touch-to-draw %d ms", SystemClock.uptimeMillis() - pendingDownTime)
            pendingDownTime = NO_PENDING
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_POINTER_DOWN -> {
                    dispatch(event, event.actionIndex, PointerAction.DOWN)
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    if (latencyLogging) pendingDownTime = event.eventTime
                }
                MotionEvent.ACTION_MOVE ->
                    for (i in 0 until event.pointerCount) dispatch(event, i, PointerAction.MOVE)
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_POINTER_UP ->
                    dispatch(event, event.actionIndex, PointerAction.UP)
                MotionEvent.ACTION_CANCEL ->
                    for (i in 0 until event.pointerCount) dispatch(event, i, PointerAction.CANCEL)
            }
        } catch (e: RuntimeException) {
            Timber.e(e, "onTouchEvent: engine failed, resetting")
            engine.reset()
        }
        scheduleTimer()
        invalidate()
        return true
    }

    public fun resetState() {
        engine.reset()
        scheduleTimer()
        invalidate()
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(timerTick)
        scheduledDeadline = KeyboardEngine.NO_DEADLINE
        super.onDetachedFromWindow()
    }

    private fun onTimer() {
        scheduledDeadline = KeyboardEngine.NO_DEADLINE
        val popupWasOpen = engine.popup.isOpen
        try {
            engine.onTick(SystemClock.uptimeMillis(), sink)
        } catch (e: RuntimeException) {
            Timber.e(e, "onTimer: engine failed, resetting")
            engine.reset()
        }
        if (!popupWasOpen && engine.popup.isOpen) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        scheduleTimer()
        invalidate()
    }

    private fun scheduleTimer() {
        val deadline = engine.nextDeadline
        if (deadline == scheduledDeadline) return
        removeCallbacks(timerTick)
        scheduledDeadline = deadline
        if (deadline != KeyboardEngine.NO_DEADLINE) {
            postDelayed(timerTick, (deadline - SystemClock.uptimeMillis()).coerceAtLeast(0L))
        }
    }

    private fun drawPopup(canvas: Canvas, shifted: Boolean) {
        val popup = engine.popup
        if (!popup.isOpen) return
        val def = geometry.spec(popup.keyIndex).def as? KeyDef.Char ?: return
        val top = popup.top
        val bottom = top + popup.cellHeight
        canvas.drawRoundRect(
            popup.left,
            top,
            popup.left + popup.count * popup.cellWidth,
            bottom,
            cornerPx,
            cornerPx,
            backgroundPaint,
        )
        for (i in 0 until popup.count) {
            val left = popup.left + i * popup.cellWidth
            val right = left + popup.cellWidth
            canvas.drawRoundRect(
                left + gapPx,
                top + gapPx,
                right - gapPx,
                bottom - gapPx,
                cornerPx,
                cornerPx,
                if (i == popup.selected) activePaint else keyPaint,
            )
            val alternate = def.alternates[i]
            val label =
                if (shifted && alternate is KeyDef.Char) alternate.shifted
                else DefaultLabels.of(alternate)
            val paint = paintFor(label)
            paint.textSize = baseSizeFor(label)
            canvas.drawText(
                label,
                (left + right) / 2f,
                (top + bottom) / 2f - (paint.ascent() + paint.descent()) / 2f,
                paint,
            )
        }
    }

    private fun dispatch(event: MotionEvent, index: Int, action: PointerAction) {
        engine.onPointer(
            event.getPointerId(index),
            event.getX(index),
            event.getY(index) - keysTop,
            action,
            event.eventTime,
            sink,
        )
    }

    private fun paintFor(label: String): Paint =
        if (label.length > 1) smallLabelPaint else labelPaint

    private fun baseSizeFor(label: String): Float =
        if (label.length > 1) smallLabelSizePx else labelSizePx

    private fun fillPaint(colorRes: Int): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, colorRes) }

    private fun labelPaint(sizeRes: Int): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.key_label)
            textAlign = Paint.Align.CENTER
            textSize = resources.getDimension(sizeRes)
        }

    private companion object {
        const val NO_PENDING = -1L
        const val LATENCY_TAG = "PCK-LAT"
    }
}
