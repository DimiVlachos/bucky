package app.buckykeys.keyboard.ime.view

internal const val LABEL_HEIGHT_FRACTION = 0.55f
internal const val KEY_LABEL_HEIGHT_FRACTION = 0.8f
internal const val PREVIEW_LABEL_HEIGHT_FRACTION = 0.7f

internal fun fittedTextSize(
    baseSize: Float,
    labelWidth: Float,
    availableWidth: Float,
    minSize: Float,
    maxSize: Float = Float.MAX_VALUE,
): Float {
    val widthFit =
        when {
            availableWidth <= 0f -> minSize
            labelWidth <= availableWidth -> baseSize
            else -> maxOf(minSize, baseSize * availableWidth / labelWidth)
        }
    return minOf(widthFit, maxSize)
}

// Scaled after fitting, so above 100% a single character may spill past its key, as in Hacker's
// Keyboard.
internal fun scaledLabelSize(fittedSize: Float, scale: Float, maxSize: Float): Float =
    minOf(fittedSize * scale, maxSize)

internal fun visibleCharCount(label: String): Int {
    var count = 0
    var i = 0
    while (i < label.length) {
        val codePoint = label.codePointAt(i)
        if (!isAttached(codePoint)) count++
        i += Character.charCount(codePoint)
    }
    return count
}

private fun isAttached(codePoint: Int): Boolean =
    when (Character.getType(codePoint).toByte()) {
        Character.NON_SPACING_MARK,
        Character.ENCLOSING_MARK,
        Character.COMBINING_SPACING_MARK,
        Character.FORMAT -> true
        else -> false
    }

// Single characters share one size so 5 and % don't shrink alone; word labels keep their own fit.
// Keys narrower than one unit (Finnish § ½) fit on their own so they don't shrink every label.
internal fun evenBigLabels(sizes: FloatArray, small: BooleanArray, narrow: BooleanArray) {
    var min = Float.MAX_VALUE
    for (i in sizes.indices) if (!small[i] && !narrow[i]) min = minOf(min, sizes[i])
    for (i in sizes.indices) {
        if (!small[i]) sizes[i] = if (narrow[i]) minOf(sizes[i], min) else min
    }
}
