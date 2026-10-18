package app.buckykeys.keyboard.ime.geometry

import app.buckykeys.keyboard.ime.layout.KeyCode
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.SpikeLayout
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardGeometryTest {

    private val layout = SpikeLayout.pcFullEnUs
    private val geometry = KeyboardGeometry(layout, widthPx = 1080, rowHeightPx = 120)

    @Test
    fun `spike layout has five rows`() {
        assertEquals(5, layout.rows.size)
    }

    @Test
    fun `the top left corner is escape`() {
        assertEquals(KeyDef.Special(KeyCode.ESC), geometry.spec(geometry.keyAt(1f, 1f)).def)
    }

    @Test
    fun `a point below the last row hits nothing`() {
        assertEquals(-1, geometry.keyAt(10f, 601f))
    }

    @Test
    fun `a point left of the keyboard hits nothing`() {
        assertEquals(-1, geometry.keyAt(-1f, 10f))
    }

    @Test
    fun `every row fills the view width`() {
        var index = 0
        layout.rows.forEach { row ->
            index += row.keys.size
            assertEquals(1080f, geometry.rects[(index - 1) * 4 + 2], 0.5f)
        }
    }

    @Test
    fun `the second key of the third row starts after the wider control key`() {
        val firstOfRow3 = layout.rows[0].keys.size + layout.rows[1].keys.size
        val unit = 1080f / layout.rows[2].keys.sumOf { it.width.toDouble() }.toFloat()
        assertEquals(1.75f * unit, geometry.rects[(firstOfRow3 + 1) * 4], 0.5f)
    }

    @Test
    fun `the up arrow sits directly above the down arrow`() {
        assertEquals(centerX(KeyCode.UP), centerX(KeyCode.DOWN), 0.5f)
    }

    @Test
    fun `delete sits directly above the right arrow`() {
        val del = indexOf(KeyCode.FORWARD_DEL)
        val right = indexOf(KeyCode.RIGHT)
        assertEquals(geometry.rects[right * 4], geometry.rects[del * 4], 0.5f)
        assertEquals(geometry.rects[right * 4 + 2], geometry.rects[del * 4 + 2], 0.5f)
    }

    @Test
    fun `a zero width view has no hits`() {
        assertEquals(-1, KeyboardGeometry(layout, widthPx = 0, rowHeightPx = 120).keyAt(0f, 0f))
    }

    @Test
    fun `a zero row height view has no hits`() {
        assertEquals(-1, KeyboardGeometry(layout, widthPx = 1080, rowHeightPx = 0).keyAt(5f, 0f))
    }

    private fun indexOf(code: KeyCode): Int =
        (0 until geometry.keyCount).first { geometry.spec(it).def == KeyDef.Special(code) }

    private fun centerX(code: KeyCode): Float {
        val index = indexOf(code)
        return (geometry.rects[index * 4] + geometry.rects[index * 4 + 2]) / 2f
    }
}
