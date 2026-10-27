package app.buckykeys.keyboard.ime.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.buckykeys.keyboard.ime.R
import app.buckykeys.keyboard.ime.engine.InputSink
import app.buckykeys.keyboard.ime.engine.KeyboardEngine
import app.buckykeys.keyboard.ime.engine.PointerAction
import app.buckykeys.keyboard.ime.geometry.KeyboardGeometry
import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.DefaultLabels
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.ime.layout.ModifierKind
import kotlin.math.roundToInt
import timber.log.Timber

public fun interface LanguageChangeListener {
    public fun onLanguageChanged(index: Int)
}

@SuppressLint("ViewConstructor")
public class KeyboardView(
    context: Context,
    languages: List<Layout>,
    initialLanguageIndex: Int,
    private val languageChangeListener: LanguageChangeListener,
    private val sink: InputSink,
    private val latencyLogging: Boolean,
    private val rowHeightPx: Int,
    private val haptics: Boolean,
    private val labelScale: Float = 1f,
    pages: Map<String, Layout> = emptyMap(),
    private val actions: KeyboardActions = KeyboardActions {},
) : View(context) {

    private val gapPx = resources.getDimension(R.dimen.key_gap)
    private val cornerPx = resources.getDimension(R.dimen.key_corner_radius)
    private val labelPaddingPx = resources.getDimension(R.dimen.key_label_padding)
    private val labelMinSizePx = resources.getDimension(R.dimen.key_label_min_size)
    private val unscaledLabelSizePx = resources.getDimension(R.dimen.key_label_size)
    private val labelSizePx = unscaledLabelSizePx * labelScale
    private val smallLabelSizePx = resources.getDimension(R.dimen.key_label_small_size) * labelScale
    private val lockBarHeightPx = resources.getDimension(R.dimen.key_lock_bar_height)
    private val lockBarInsetPx = resources.getDimension(R.dimen.key_lock_bar_inset)
    private val labelMaxSizePx = rowHeightPx * KEY_LABEL_HEIGHT_FRACTION

    public val keysTop: Int = rowHeightPx

    private val keysEdgeGapPx = gapPx.roundToInt()

    private val edgeBandPx = keysEdgeGapPx + gapPx

    private val longPressMs = ViewConfiguration.getLongPressTimeout().toLong()

    private val backgroundPaint = fillPaint(R.color.keyboard_background)
    private val previewLabelPaint =
        labelPaint(
            minOf(
                resources.getDimension(R.dimen.key_preview_label_size),
                rowHeightPx * PREVIEW_LABEL_HEIGHT_FRACTION,
            ),
        )

    public var previewEnabled: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private val keyPaint = fillPaint(R.color.key_background)
    private val specialKeyPaint = fillPaint(R.color.key_special_background)
    private val pressedPaint = fillPaint(R.color.key_pressed)
    private val activePaint = fillPaint(R.color.key_modifier_active)
    private val lockedPaint = fillPaint(R.color.key_modifier_locked)
    private val lockBarPaint = fillPaint(R.color.key_label)
    private val labelPaint = labelPaint(labelSizePx)
    private val smallLabelPaint = labelPaint(smallLabelSizePx)
    private val hintPaint =
        labelPaint(
                minOf(
                    resources.getDimension(R.dimen.key_hint_size),
                    rowHeightPx * HINT_HEIGHT_FRACTION,
                ),
            )
            .apply {
                color = ContextCompat.getColor(context, R.color.key_label_secondary)
                textAlign = Paint.Align.RIGHT
            }
    private val spaceLabelPaint =
        labelPaint(smallLabelSizePx).apply {
            color = ContextCompat.getColor(context, R.color.key_label_secondary)
        }

    private val pageSet = PageSet(languages, pages)
    private var languageIndex = initialLanguageIndex.coerceIn(0, pageSet.languageCount - 1)
    private var page = BuiltInLayouts.PAGE_LETTERS
    private var pendingPage = BuiltInLayouts.PAGE_LETTERS
    private var pendingAction: String? = null
    private val layout: Layout
        get() = pageSet.current(languageIndex, page)

    private var geometry = KeyboardGeometry(layout, 0, rowHeightPx)
    private var geometryLanguage = languageIndex
    private var geometryPage = page
    private val engine = KeyboardEngine(geometry, longPressMs)
    private var labelX = FloatArray(0)
    private var labelY = FloatArray(0)
    private var labelSize = FloatArray(0)
    private var labelSmall = BooleanArray(0)
    private var modifierBits = IntArray(0)
    private var special = BooleanArray(0)
    private var hintLabels = emptyArray<String>()
    private var fnLabels = emptyArray<String>()
    private var fnSmall = BooleanArray(0)
    private var fnSize = FloatArray(0)
    private var fnY = FloatArray(0)
    private var hintX = FloatArray(0)
    private var hintY = FloatArray(0)
    private var spaceKey = NO_SPACE_KEY
    private var spaceLabelSize = 0f
    private var spaceLabelY = 0f
    private var popupLabels = emptyArray<String>()
    private var popupShiftedLabels = emptyArray<String>()
    private var popupSmall = BooleanArray(0)
    private var popupSize = FloatArray(0)
    private var pendingDownTime = NO_PENDING
    private var lastLatencyMs = 0L
    private var scheduledDeadline = KeyboardEngine.NO_DEADLINE
    private val timerTick = Runnable { onTimer() }
    private val nextLanguage = Runnable { guarded { switchLanguage(1) } }
    private val previousLanguage = Runnable { guarded { switchLanguage(-1) } }
    private val pageSwitch = Runnable { guarded { switchPage(pendingPage) } }
    private val fnPageSync = Runnable { guarded { syncFnPage() } }
    private val actionRequest = Runnable {
        guarded { pendingAction?.let { actions.onAction(it) } }
        pendingAction = null
    }
    private val relayout = Runnable { requestLayout() }
    private val logLatency = Runnable {
        Timber.tag(LATENCY_TAG).d("touch-to-draw %d ms", lastLatencyMs)
    }

    init {
        engine.languageSwipe = pageSet.languageCount > 1
        buildLabels()
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            applyInsets(insets)
            insets
        }
    }

    private fun applyInsets(insets: WindowInsetsCompat) {
        val bars =
            insets.getInsets(
                WindowInsetsCompat.Type.navigationBars() or
                    WindowInsetsCompat.Type.tappableElement() or
                    WindowInsetsCompat.Type.displayCutout(),
            )
        val gestures = insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures())
        val bottom = maxOf(bars.bottom, gestures.bottom)
        val sidesChanged = bars.left != paddingLeft || bars.right != paddingRight
        val bottomChanged = bottom != paddingBottom
        setPadding(bars.left, 0, bars.right, bottom)
        if (sidesChanged && width > 0) rebuildGeometryIfNeeded()
        // Insets that arrive mid-traversal only remeasure inside the current IME window; a fresh
        // layout pass lets the window grow to the new height.
        if (bottomChanged && isAttachedToWindow) {
            removeCallbacks(relayout)
            post(relayout)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            keysTop +
                keysEdgeGapPx +
                layout.rows.size * rowHeightPx +
                maxOf(paddingBottom, keysEdgeGapPx),
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        rebuildGeometryIfNeeded()
    }

    private fun lastKeyX(): Float = (geometry.widthPx - 1).coerceAtLeast(0).toFloat()

    private fun rebuildGeometryIfNeeded(keepPointers: Boolean = false) {
        val contentWidth = (width - paddingLeft - paddingRight - 2 * keysEdgeGapPx).coerceAtLeast(0)
        if (
            contentWidth == geometry.widthPx &&
                geometryLanguage == languageIndex &&
                geometryPage == page
        ) {
            return
        }
        geometry = KeyboardGeometry(layout, contentWidth, rowHeightPx)
        geometryLanguage = languageIndex
        geometryPage = page
        try {
            engine.setGeometry(geometry, keepPointers)
        } catch (e: RuntimeException) {
            Timber.e(e, "setGeometry: engine failed, resetting")
            engine.reset()
        }
        buildLabels()
        scheduleTimer()
    }

    private fun buildLabels() {
        val count = geometry.keyCount
        labelX = FloatArray(count)
        labelY = FloatArray(count)
        labelSize = FloatArray(count)
        labelSmall = BooleanArray(count)
        modifierBits = IntArray(count)
        special = BooleanArray(count)
        spaceKey = NO_SPACE_KEY
        val rects = geometry.rects
        hintLabels = Array(count) { hintFor(geometry.spec(it).def) }
        fnLabels = Array(count) { geometry.spec(it).fn?.let(DefaultLabels::of).orEmpty() }
        fnSmall = BooleanArray(count) { isSmall(fnLabels[it]) }
        fnSize = FloatArray(count)
        fnY = FloatArray(count)
        hintX = FloatArray(count) { rects[it * 4 + 2] - gapPx - 2 * labelPaddingPx }
        hintY =
            FloatArray(count) { rects[it * 4 + 1] + gapPx + labelPaddingPx - hintPaint.ascent() }
        for (i in 0 until count) {
            val spec = geometry.spec(i)
            val def = spec.def
            val shiftedLabel =
                when (def) {
                    is KeyDef.Char -> def.shifted
                    is KeyDef.Dead -> def.shifted
                    else -> spec.label
                }
            labelSmall[i] = isSmall(spec.label)
            val paint = if (labelSmall[i]) smallLabelPaint else labelPaint
            val cellWidth = rects[i * 4 + 2] - rects[i * 4]
            labelSize[i] =
                if (labelSmall[i]) {
                    fitLabel(
                        paint,
                        smallLabelSizePx,
                        spec.label,
                        shiftedLabel,
                        cellWidth,
                        labelMaxSizePx,
                    )
                } else {
                    bigLabelSize(paint, spec.label, shiftedLabel, cellWidth)
                }
            paint.textSize = labelSize[i]
            labelX[i] = (rects[i * 4] + rects[i * 4 + 2]) / 2f
            labelY[i] =
                (rects[i * 4 + 1] + rects[i * 4 + 3]) / 2f - (paint.ascent() + paint.descent()) / 2f
            modifierBits[i] = if (def is KeyDef.Modifier) 1 shl def.kind.ordinal else 0
            special[i] = isSpecialKey(def)
            if (showsLanguage() && def is KeyDef.Char && def.primary == " ") spaceKey = i
            if (fnLabels[i].isNotEmpty()) buildFnLabel(i, rects)
        }
        evenBigLabels(labelSize, BooleanArray(count) { labelSmall[it] || special[it] })
        recomputeY(labelSize, labelSmall, labelY, rects) { true }
        val fnExcluded = BooleanArray(count) { fnSmall[it] || fnLabels[it].isEmpty() }
        evenBigLabels(fnSize, fnExcluded)
        recomputeY(fnSize, fnSmall, fnY, rects) { fnLabels[it].isNotEmpty() }
        if (spaceKey != NO_SPACE_KEY) buildSpaceLabel(rects)
    }

    // One size per label class, so a key whose shifted form is wider (5 and %) doesn't shrink
    // alone.
    private inline fun recomputeY(
        sizes: FloatArray,
        small: BooleanArray,
        ys: FloatArray,
        rects: FloatArray,
        included: (Int) -> Boolean,
    ) {
        for (i in sizes.indices) {
            if (!included(i)) continue
            val paint = if (small[i]) smallLabelPaint else labelPaint
            paint.textSize = sizes[i]
            ys[i] =
                (rects[i * 4 + 1] + rects[i * 4 + 3]) / 2f - (paint.ascent() + paint.descent()) / 2f
        }
    }

    private fun buildFnLabel(i: Int, rects: FloatArray) {
        val paint = if (fnSmall[i]) smallLabelPaint else labelPaint
        val cellWidth = rects[i * 4 + 2] - rects[i * 4]
        fnSize[i] =
            if (fnSmall[i]) {
                fitLabel(
                    paint,
                    smallLabelSizePx,
                    fnLabels[i],
                    fnLabels[i],
                    cellWidth,
                    labelMaxSizePx,
                )
            } else {
                bigLabelSize(paint, fnLabels[i], fnLabels[i], cellWidth)
            }
        paint.textSize = fnSize[i]
        fnY[i] =
            (rects[i * 4 + 1] + rects[i * 4 + 3]) / 2f - (paint.ascent() + paint.descent()) / 2f
    }

    private fun showsLanguage(): Boolean =
        engine.languageSwipe && page == BuiltInLayouts.PAGE_LETTERS

    private fun buildSpaceLabel(rects: FloatArray) {
        val i = spaceKey
        spaceLabelSize =
            fitLabel(
                spaceLabelPaint,
                smallLabelSizePx,
                layout.language,
                layout.language,
                rects[i * 4 + 2] - rects[i * 4],
                labelMaxSizePx,
            )
        spaceLabelPaint.textSize = spaceLabelSize
        spaceLabelY =
            (rects[i * 4 + 1] + rects[i * 4 + 3]) / 2f -
                (spaceLabelPaint.ascent() + spaceLabelPaint.descent()) / 2f
    }

    private fun fitLabel(
        paint: Paint,
        baseSize: Float,
        label: String,
        shiftedLabel: String,
        cellWidth: Float,
        maxSize: Float,
    ): Float {
        paint.textSize = baseSize
        return fittedTextSize(
            baseSize,
            maxOf(paint.measureText(label), paint.measureText(shiftedLabel)),
            cellWidth - 2 * (gapPx + labelPaddingPx),
            labelMinSizePx,
            maxSize,
        )
    }

    private fun bigLabelSize(
        paint: Paint,
        label: String,
        shiftedLabel: String,
        cellWidth: Float,
    ): Float =
        scaledLabelSize(
            fitLabel(paint, unscaledLabelSizePx, label, shiftedLabel, cellWidth, Float.MAX_VALUE),
            labelScale,
            labelMaxSizePx,
        )

    private fun buildPopupLabels() {
        val popup = engine.popup
        val def = geometry.spec(popup.keyIndex).def as? KeyDef.Char ?: return
        val count = popup.count
        popupLabels = Array(count) { DefaultLabels.of(def.alternates[it]) }
        popupShiftedLabels =
            Array(count) {
                when (val alternate = def.alternates[it]) {
                    is KeyDef.Char -> alternate.shifted
                    else -> popupLabels[it]
                }
            }
        popupSmall = BooleanArray(count) { isSmall(popupLabels[it]) }
        popupSize =
            FloatArray(count) {
                fitLabel(
                    if (popupSmall[it]) smallLabelPaint else labelPaint,
                    if (popupSmall[it]) smallLabelSizePx else labelSizePx,
                    popupLabels[it],
                    popupShiftedLabels[it],
                    popup.cellWidth,
                    popup.cellHeight * LABEL_HEIGHT_FRACTION,
                )
            }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, keysTop.toFloat(), width.toFloat(), height.toFloat(), backgroundPaint)
        if (labelX.size != geometry.keyCount) return
        canvas.save()
        canvas.translate(
            (paddingLeft + keysEdgeGapPx).toFloat(),
            (keysTop + keysEdgeGapPx).toFloat(),
        )
        val rects = geometry.rects
        val mask = engine.modifierMask
        val locked = engine.lockedMask
        val shifted = mask and (1 shl ModifierKind.SHIFT.ordinal) != 0
        val deadShifted = engine.deadKeyShifted
        val fnActive = mask and (1 shl ModifierKind.FN.ordinal) != 0
        for (i in 0 until geometry.keyCount) {
            val fill =
                when {
                    engine.isPressed(i) -> pressedPaint
                    i == engine.pendingDeadKey -> activePaint
                    modifierBits[i] and locked != 0 -> lockedPaint
                    modifierBits[i] and mask != 0 -> activePaint
                    special[i] -> specialKeyPaint
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
            drawLabel(canvas, i, shifted, deadShifted, fnActive)
            if (modifierBits[i] and locked != 0) drawLockBar(canvas, i)
        }
        drawPreview(canvas, shifted)
        drawPopup(canvas, shifted)
        canvas.restore()
        if (pendingDownTime != NO_PENDING) {
            lastLatencyMs = SystemClock.uptimeMillis() - pendingDownTime
            pendingDownTime = NO_PENDING
            post(logLatency)
        }
    }

    private fun drawLabel(
        canvas: Canvas,
        i: Int,
        shifted: Boolean,
        deadShifted: Boolean,
        fnActive: Boolean,
    ) {
        if (fnActive && fnLabels[i].isNotEmpty()) {
            val paint = if (fnSmall[i]) smallLabelPaint else labelPaint
            paint.textSize = fnSize[i]
            canvas.drawText(fnLabels[i], labelX[i], fnY[i], paint)
            return
        }
        if (i == spaceKey) {
            spaceLabelPaint.textSize = spaceLabelSize
            canvas.drawText(layout.language, labelX[i], spaceLabelY, spaceLabelPaint)
            return
        }
        val spec = geometry.spec(i)
        val label =
            when (val def = spec.def) {
                is KeyDef.Char -> if (shifted) def.shifted else spec.label
                is KeyDef.Dead -> if (deadShifted) def.shifted else spec.label
                else -> spec.label
            }
        val paint = if (labelSmall[i]) smallLabelPaint else labelPaint
        paint.textSize = labelSize[i]
        canvas.drawText(label, labelX[i], labelY[i], paint)
        if (hintLabels[i].isNotEmpty())
            canvas.drawText(hintLabels[i], hintX[i], hintY[i], hintPaint)
    }

    private fun drawLockBar(canvas: Canvas, key: Int) {
        val rects = geometry.rects
        val halfWidth = (rects[key * 4 + 2] - rects[key * 4]) * LOCK_BAR_WIDTH_FRACTION / 2f
        val bottom = rects[key * 4 + 3] - gapPx - lockBarInsetPx
        canvas.drawRect(
            labelX[key] - halfWidth,
            bottom - lockBarHeightPx,
            labelX[key] + halfWidth,
            bottom,
            lockBarPaint,
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_POINTER_DOWN -> {
                    dispatch(event, event.actionIndex, PointerAction.DOWN)
                    if (haptics) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
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
            handleSwitchRequest()
            requestFnPageSync()
        } catch (e: RuntimeException) {
            Timber.e(e, "onTouchEvent: engine failed, resetting")
            engine.reset()
        }
        scheduleTimer()
        invalidate()
        return true
    }

    private inline fun guarded(block: () -> Unit) {
        try {
            block()
        } catch (e: RuntimeException) {
            Timber.e(e, "keyboard switch failed, resetting")
            engine.reset()
            invalidate()
        }
    }

    public fun setAutoShift(on: Boolean) {
        engine.setAutoShift(on)
        invalidate()
    }

    public fun resetState() {
        engine.reset()
        removeCallbacks(pageSwitch)
        removeCallbacks(fnPageSync)
        removeCallbacks(actionRequest)
        removeCallbacks(nextLanguage)
        removeCallbacks(previousLanguage)
        showPage(BuiltInLayouts.PAGE_LETTERS)
        scheduleTimer()
        invalidate()
    }

    // Swapping builds a new geometry, so touch and timer paths post it instead of allocating there.
    private fun requestFnPageSync() {
        if (fnTarget() == page) return
        removeCallbacks(fnPageSync)
        post(fnPageSync)
    }

    private fun syncFnPage() {
        val target = fnTarget()
        if (target != page) switchPage(target)
    }

    private fun fnTarget(): String {
        val fnActive = engine.modifierMask and (1 shl ModifierKind.FN.ordinal) != 0
        return fnPageTarget(page, fnActive, pageSet.isPage(BuiltInLayouts.PAGE_FN))
    }

    public fun showPage(target: String) {
        if (page == target || !pageSet.isPage(target)) return
        switchPage(target)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // The first measure sizes the IME window, and the listener only runs after it.
        ViewCompat.getRootWindowInsets(this)?.let(::applyInsets)
        ViewCompat.requestApplyInsets(this)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(timerTick)
        removeCallbacks(nextLanguage)
        removeCallbacks(previousLanguage)
        removeCallbacks(pageSwitch)
        removeCallbacks(fnPageSync)
        removeCallbacks(actionRequest)
        removeCallbacks(logLatency)
        removeCallbacks(relayout)
        scheduledDeadline = KeyboardEngine.NO_DEADLINE
        super.onDetachedFromWindow()
    }

    private fun handleSwitchRequest() {
        val target = engine.takeSwitchRequest() ?: return
        when (target) {
            BuiltInLayouts.NEXT_LANGUAGE,
            BuiltInLayouts.PREVIOUS_LANGUAGE -> switchLanguageLater(target)
            BuiltInLayouts.OTHER_KEYBOARD,
            BuiltInLayouts.FULL_LAYOUT -> {
                pendingAction = target
                post(actionRequest)
            }
            else ->
                if (pageSet.isPage(target)) {
                    pendingPage = target
                    post(pageSwitch)
                } else {
                    Timber.w("no layout for switch target %s", target)
                }
        }
    }

    private fun switchLanguageLater(target: String) {
        when {
            pageSet.languageCount < 2 -> Timber.w("no other language for switch target %s", target)
            target == BuiltInLayouts.NEXT_LANGUAGE -> post(nextLanguage)
            else -> post(previousLanguage)
        }
    }

    private fun switchPage(target: String) {
        val rows = layout.rows.size
        page = target
        rebuildGeometryIfNeeded(keepPointers = true)
        if (layout.rows.size != rows) requestLayout()
        invalidate()
    }

    private fun switchLanguage(step: Int) {
        languageIndex = (languageIndex + step).mod(pageSet.languageCount)
        page = BuiltInLayouts.PAGE_LETTERS
        rebuildGeometryIfNeeded()
        syncFnPage()
        languageChangeListener.onLanguageChanged(languageIndex)
        requestLayout()
        invalidate()
    }

    private fun onTimer() {
        scheduledDeadline = KeyboardEngine.NO_DEADLINE
        val longPresses = engine.longPressCount
        try {
            engine.onTick(SystemClock.uptimeMillis(), sink)
            if (engine.longPressCount != longPresses) {
                if (engine.popup.isOpen) buildPopupLabels()
                if (haptics) performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            handleSwitchRequest()
            requestFnPageSync()
        } catch (e: RuntimeException) {
            Timber.e(e, "onTimer: engine failed, resetting")
            engine.reset()
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

    private fun drawPreview(canvas: Canvas, shifted: Boolean) {
        val key = engine.previewKey
        if (!previewEnabled || key == KeyboardEngine.NO_PREVIEW) return
        val spec = geometry.spec(key)
        val def = spec.def as? KeyDef.Char ?: return
        val label = if (shifted) def.shifted else spec.label
        if (label.isBlank()) return
        val rects = geometry.rects
        val keyLeft = rects[key * 4]
        val keyRight = rects[key * 4 + 2]
        val previewWidth = maxOf(keyRight - keyLeft, rowHeightPx.toFloat())
        val left =
            ((keyLeft + keyRight - previewWidth) / 2f)
                .coerceAtMost(geometry.widthPx - previewWidth)
                .coerceAtLeast(0f)
        val bottom = rects[key * 4 + 1]
        val top = bottom - rowHeightPx
        canvas.drawRoundRect(
            left + gapPx,
            top + gapPx,
            left + previewWidth - gapPx,
            bottom - gapPx,
            cornerPx,
            cornerPx,
            pressedPaint,
        )
        canvas.drawText(
            label,
            left + previewWidth / 2f,
            (top + bottom) / 2f - (previewLabelPaint.ascent() + previewLabelPaint.descent()) / 2f,
            previewLabelPaint,
        )
    }

    private fun drawPopup(canvas: Canvas, shifted: Boolean) {
        val popup = engine.popup
        if (!popup.isOpen) return
        if (popupLabels.size != popup.count) return
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
        for (cell in 0 until popup.count) {
            val i = popup.alternateAt(cell)
            val left = popup.left + cell * popup.cellWidth
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
            val label = if (shifted) popupShiftedLabels[i] else popupLabels[i]
            val paint = if (popupSmall[i]) smallLabelPaint else labelPaint
            paint.textSize = popupSize[i]
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
            edgeClamp(event.getX(index) - paddingLeft - keysEdgeGapPx, lastKeyX(), edgeBandPx),
            edgeClamp(event.getY(index) - keysTop - keysEdgeGapPx, Float.MAX_VALUE, edgeBandPx),
            action,
            event.eventTime,
            sink,
        )
    }

    private fun isSmall(label: String): Boolean = visibleCharCount(label) > 1

    private fun fillPaint(colorRes: Int): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, colorRes) }

    private fun labelPaint(sizePx: Float): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.key_label)
            textAlign = Paint.Align.CENTER
            textSize = sizePx
        }

    private companion object {
        const val NO_PENDING = -1L
        const val NO_SPACE_KEY = -1
        const val HINT_HEIGHT_FRACTION = 0.2f
        const val LATENCY_TAG = "PCK-LAT"
        const val LOCK_BAR_WIDTH_FRACTION = 0.3f
    }
}
