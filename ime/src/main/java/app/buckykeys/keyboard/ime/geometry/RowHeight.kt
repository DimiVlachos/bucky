package app.buckykeys.keyboard.ime.geometry

public fun cappedRowHeightPx(
    chosenPx: Int,
    rows: Int,
    screenHeightPx: Int,
    landscape: Boolean,
): Int {
    if (rows <= 0 || screenHeightPx <= 0) return chosenPx
    val fraction = if (landscape) LANDSCAPE_MAX_FRACTION else PORTRAIT_MAX_FRACTION
    val cap = (screenHeightPx * fraction / rows).toInt()
    return minOf(chosenPx, cap).coerceAtLeast(1)
}

private const val LANDSCAPE_MAX_FRACTION = 0.5f
private const val PORTRAIT_MAX_FRACTION = 0.6f

public fun compactRowHeightPx(fullRowPx: Int): Int = fullRowPx * FULL_MAIN_ROWS / COMPACT_ROWS

private const val FULL_MAIN_ROWS = 5
private const val COMPACT_ROWS = 4
