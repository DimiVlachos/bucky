package dev.pckeys.keyboard.ime.geometry

import dev.pckeys.keyboard.ime.layout.KeySpec
import dev.pckeys.keyboard.ime.layout.Layout

public class KeyboardGeometry(layout: Layout, widthPx: Int, private val rowHeightPx: Int) {

    private val specs: List<KeySpec> = layout.rows.flatMap { it.keys }
    private val rowStarts: IntArray = IntArray(layout.rows.size + 1)
    private val hittable: Boolean = widthPx > 0 && rowHeightPx > 0

    public val keyCount: Int = specs.size
    public val rects: FloatArray = FloatArray(keyCount * 4)

    init {
        var index = 0
        layout.rows.forEachIndexed { rowIndex, row ->
            rowStarts[rowIndex] = index
            val unit = widthPx / row.keys.sumOf { it.width.toDouble() }.toFloat()
            val top = rowIndex * rowHeightPx.toFloat()
            var left = 0f
            row.keys.forEach { key ->
                val right = left + key.width * unit
                rects[index * 4] = left
                rects[index * 4 + 1] = top
                rects[index * 4 + 2] = right
                rects[index * 4 + 3] = top + rowHeightPx
                left = right
                index++
            }
        }
        rowStarts[layout.rows.size] = index
    }

    public fun spec(index: Int): KeySpec = specs[index]

    public fun keyAt(x: Float, y: Float): Int {
        if (!hittable || x < 0f || y < 0f) return -1
        val row = (y / rowHeightPx).toInt()
        if (row >= rowStarts.size - 1) return -1
        for (index in rowStarts[row] until rowStarts[row + 1]) {
            if (x >= rects[index * 4] && x < rects[index * 4 + 2]) return index
        }
        return -1
    }
}
