package app.buckykeys.keyboard.ime.engine

public class AlternatesPopup internal constructor() {

    public var keyIndex: Int = NONE
        private set

    public var count: Int = 0
        private set

    public var selected: Int = NONE
        private set

    public var left: Float = 0f
        private set

    public var top: Float = 0f
        private set

    public var cellWidth: Float = 0f
        private set

    public var cellHeight: Float = 0f
        private set

    public var reversed: Boolean = false
        private set

    private var keyBottom = 0f

    public val isOpen: Boolean
        get() = keyIndex != NONE

    internal fun open(keyIndex: Int, count: Int, rects: FloatArray, keyboardWidth: Float) {
        val i = keyIndex * 4
        this.keyIndex = keyIndex
        this.count = count
        cellWidth = minOf(rects[i + 2] - rects[i], keyboardWidth / count)
        cellHeight = rects[i + 3] - rects[i + 1]
        val width = count * cellWidth
        reversed = rects[i] + width > keyboardWidth
        left =
            (if (reversed) rects[i + 2] - width else rects[i]).coerceIn(0f, keyboardWidth - width)
        top = rects[i + 1] - cellHeight
        keyBottom = rects[i + 3]
        select((rects[i] + rects[i + 2]) / 2f, rects[i + 1])
    }

    internal fun select(x: Float, y: Float) {
        selected =
            if (y > keyBottom) {
                NONE
            } else {
                alternateAt(((x - left) / cellWidth).toInt().coerceIn(0, count - 1))
            }
    }

    public fun alternateAt(cell: Int): Int = if (reversed) count - 1 - cell else cell

    internal fun close() {
        keyIndex = NONE
        count = 0
        selected = NONE
        reversed = false
    }

    public companion object {
        public const val NONE: Int = -1
    }
}
