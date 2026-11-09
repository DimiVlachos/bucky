package app.buckykeys.keyboard.ime.engine

// How far, in row heights, this person's taps land from the middle of the key they type. Held by
// the service so it outlives keyboard rebuilds.
public class TouchBias {

    public var x: Float = 0f
        private set

    public var y: Float = 0f
        private set

    internal fun learn(dx: Float, dy: Float) {
        x = (x + (dx - x) * RATE).coerceIn(-MAX_ROWS, MAX_ROWS)
        y = (y + (dy - y) * RATE).coerceIn(-MAX_ROWS, MAX_ROWS)
    }

    internal companion object {
        const val RATE = 0.05f
        const val MAX_ROWS = 0.3f
    }
}
