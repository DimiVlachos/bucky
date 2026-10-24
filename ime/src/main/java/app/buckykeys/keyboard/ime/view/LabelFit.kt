package app.buckykeys.keyboard.ime.view

internal const val LABEL_HEIGHT_FRACTION = 0.55f
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
