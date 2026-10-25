package app.buckykeys.keyboard.ime.view

// Touches in the edge gap belong to the edge key; further out they stay outside so the key cancels.
internal fun edgeClamp(value: Float, max: Float, band: Float): Float =
    when {
        value < 0f && value >= -band -> 0f
        value > max && value <= max + band -> max
        else -> value
    }
