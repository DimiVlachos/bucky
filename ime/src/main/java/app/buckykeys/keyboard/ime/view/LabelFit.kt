package app.buckykeys.keyboard.ime.view

internal fun fittedTextSize(
    baseSize: Float,
    labelWidth: Float,
    availableWidth: Float,
    minSize: Float,
): Float =
    when {
        availableWidth <= 0f -> minSize
        labelWidth <= availableWidth -> baseSize
        else -> maxOf(minSize, baseSize * availableWidth / labelWidth)
    }
