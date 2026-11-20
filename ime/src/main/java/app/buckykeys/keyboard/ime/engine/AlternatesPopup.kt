package app.buckykeys.keyboard.ime.engine

internal class AlternatesPopup {

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

    public var rows: Int = 1
        private set

    public var columns: Int = 0
        private set

    public var cellWidth: Float = 0f
        private set

    public var cellHeight: Float = 0f
        private set

    public var reversed: Boolean = false
        private set

    private var keyTop = 0f

    private var keyBottom = 0f

    private var anchor = 0

    public val isOpen: Boolean
        get() = keyIndex != NONE

    internal fun open(keyIndex: Int, count: Int, rects: FloatArray, keyboardWidth: Float) {
        val i = keyIndex * 4
        this.keyIndex = keyIndex
        this.count = count
        rows = (count + MAX_COLUMNS - 1) / MAX_COLUMNS
        columns = (count + rows - 1) / rows
        cellHeight = rects[i + 3] - rects[i + 1]
        // Narrow keys get square cells so the alternates read as large as the keys, as ketas asked.
        cellWidth = minOf(maxOf(rects[i + 2] - rects[i], cellHeight), keyboardWidth / columns)
        val width = columns * cellWidth
        val centre = (rects[i] + rects[i + 2]) / 2f
        left = (centre - cellWidth / 2f).coerceAtMost(keyboardWidth - width).coerceAtLeast(0f)
        anchor = ((centre - left) / cellWidth).toInt().coerceIn(0, columns - 1)
        reversed = columns - 1 - anchor < anchor
        keyTop = rects[i + 1]
        top = keyTop - rows * cellHeight
        keyBottom = rects[i + 3]
        select(centre, keyTop)
    }

    internal fun select(x: Float, y: Float) {
        selected =
            if (y > keyBottom) {
                NONE
            } else {
                val row = ((keyTop - y) / cellHeight).toInt().coerceIn(0, rows - 1)
                alternateAt(row, ((x - left) / cellWidth).toInt().coerceIn(0, columns - 1))
            }
    }

    public fun rowTop(row: Int): Float = keyTop - (row + 1) * cellHeight

    // Row 0 sits on the key. In each row the first alternate sits under the finger and the rest
    // fill toward the wider side, then wrap.
    public fun alternateAt(row: Int, cell: Int): Int {
        val inRow =
            when {
                !reversed && cell >= anchor -> cell - anchor
                !reversed -> columns - anchor + (anchor - 1 - cell)
                cell <= anchor -> anchor - cell
                else -> anchor + 1 + (cell - anchor - 1)
            }
        val index = row * columns + inRow
        return if (index < count) index else NONE
    }

    internal fun close() {
        keyIndex = NONE
        count = 0
        selected = NONE
        reversed = false
        anchor = 0
        rows = 1
        columns = 0
    }

    public companion object {
        public const val NONE: Int = -1

        private const val MAX_COLUMNS = 6
    }
}
