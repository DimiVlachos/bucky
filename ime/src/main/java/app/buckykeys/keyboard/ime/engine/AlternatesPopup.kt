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

    private var anchor = 0

    public val isOpen: Boolean
        get() = keyIndex != NONE

    internal fun open(keyIndex: Int, count: Int, rects: FloatArray, keyboardWidth: Float) {
        val i = keyIndex * 4
        this.keyIndex = keyIndex
        this.count = count
        cellWidth = minOf(rects[i + 2] - rects[i], keyboardWidth / count)
        cellHeight = rects[i + 3] - rects[i + 1]
        val width = count * cellWidth
        val centre = (rects[i] + rects[i + 2]) / 2f
        left = rects[i].coerceAtMost(keyboardWidth - width).coerceAtLeast(0f)
        anchor = ((centre - left) / cellWidth).toInt().coerceIn(0, count - 1)
        reversed = count - 1 - anchor < anchor
        top = rects[i + 1] - cellHeight
        keyBottom = rects[i + 3]
        select(centre, rects[i + 1])
    }

    internal fun select(x: Float, y: Float) {
        selected =
            if (y > keyBottom) {
                NONE
            } else {
                alternateAt(((x - left) / cellWidth).toInt().coerceIn(0, count - 1))
            }
    }

    // The first alternate sits under the finger; the rest fill toward the wider side, then wrap.
    public fun alternateAt(cell: Int): Int =
        when {
            !reversed && cell >= anchor -> cell - anchor
            !reversed -> count - anchor + (anchor - 1 - cell)
            cell <= anchor -> anchor - cell
            else -> anchor + 1 + (cell - anchor - 1)
        }

    internal fun close() {
        keyIndex = NONE
        count = 0
        selected = NONE
        reversed = false
        anchor = 0
    }

    public companion object {
        public const val NONE: Int = -1
    }
}
