package app.buckykeys.keyboard.ime.view

// Older Android keeps the nav bar clear of the IME window itself but still reports its inset.
internal fun keysBottomPadding(barBottom: Int, gestureBottom: Int, reservedBelow: Int): Int =
    (maxOf(barBottom, gestureBottom) - reservedBelow).coerceAtLeast(0)
