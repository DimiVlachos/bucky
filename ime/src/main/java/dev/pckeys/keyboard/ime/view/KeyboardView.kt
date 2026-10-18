package dev.pckeys.keyboard.ime.view

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
import dev.pckeys.keyboard.ime.R
import dev.pckeys.keyboard.ime.engine.InputSink
import dev.pckeys.keyboard.ime.engine.KeyboardEngine
import dev.pckeys.keyboard.ime.engine.PointerAction
import dev.pckeys.keyboard.ime.geometry.KeyboardGeometry
import dev.pckeys.keyboard.ime.layout.KeyDef
import dev.pckeys.keyboard.ime.layout.Layout
import dev.pckeys.keyboard.ime.layout.ModifierKind
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

    private val keyPaint = fillPaint(R.color.key_background)
    private val pressedPaint = fillPaint(R.color.key_pressed)
    private val activePaint = fillPaint(R.color.key_modifier_active)
    private val labelPaint = labelPaint(R.dimen.key_label_size)
    private val smallLabelPaint = labelPaint(R.dimen.key_label_small_size)

    private var geometry = KeyboardGeometry(layout, 0, rowHeightPx)
    private var engine = KeyboardEngine(geometry)
    private var labelX = FloatArray(0)
    private var labelY = FloatArray(0)
    private var modifierBits = IntArray(0)
    private var pendingDownTime = NO_PENDING

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.keyboard_background))
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
            layout.rows.size * rowHeightPx + paddingBottom,
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        geometry = KeyboardGeometry(layout, w, rowHeightPx)
        engine = KeyboardEngine(geometry)
        val count = geometry.keyCount
        labelX = FloatArray(count)
        labelY = FloatArray(count)
        modifierBits = IntArray(count)
        val rects = geometry.rects
        for (i in 0 until count) {
            val paint = paintFor(geometry.spec(i).label)
            labelX[i] = (rects[i * 4] + rects[i * 4 + 2]) / 2f
            labelY[i] =
                (rects[i * 4 + 1] + rects[i * 4 + 3]) / 2f - (paint.ascent() + paint.descent()) / 2f
            val def = geometry.spec(i).def
            modifierBits[i] = if (def is KeyDef.Modifier) 1 shl def.kind.ordinal else 0
        }
    }

    override fun onDraw(canvas: Canvas) {
        val rects = geometry.rects
        val mask = engine.modifierMask
        val shifted = mask and (1 shl ModifierKind.SHIFT.ordinal) != 0
        for (i in 0 until geometry.keyCount) {
            val fill =
                when {
                    engine.isPressed(i) -> pressedPaint
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
            canvas.drawText(label, labelX[i], labelY[i], paintFor(spec.label))
        }
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
        invalidate()
        return true
    }

    public fun resetState() {
        engine.reset()
        invalidate()
    }

    private fun dispatch(event: MotionEvent, index: Int, action: PointerAction) {
        engine.onPointer(
            event.getPointerId(index),
            event.getX(index),
            event.getY(index),
            action,
            sink,
        )
    }

    private fun paintFor(label: String): Paint =
        if (label.length > 1) smallLabelPaint else labelPaint

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
