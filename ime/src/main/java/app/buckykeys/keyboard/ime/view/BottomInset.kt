package app.buckykeys.keyboard.ime.view

// Older Android keeps the nav bar clear of the IME window but still reports its inset; a view
// that briefly overshoots its root reports a negative reservation, which must not add padding.
internal fun keysBottomPadding(barBottom: Int, gestureBottom: Int, reservedBelow: Int): Int {
    val inset = maxOf(barBottom, gestureBottom, 0)
    return inset - reservedBelow.coerceIn(0, inset)
}
