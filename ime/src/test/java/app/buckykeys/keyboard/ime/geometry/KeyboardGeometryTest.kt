package app.buckykeys.keyboard.ime.geometry

import app.buckykeys.keyboard.ime.layout.FallbackLayout
import app.buckykeys.keyboard.ime.layout.KeyCode
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.KeySpec
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.ime.layout.Row
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardGeometryTest {

    private val layout = FallbackLayout.pcFullEnUs
    private val geometry = KeyboardGeometry(layout, widthPx = 1080, rowHeightPx = 120)

    @Test
    fun `spike layout has five rows`() {
        assertEquals(5, layout.rows.size)
    }

    @Test
    fun `the top left corner is the backtick, as in hacker's keyboard`() {
        assertEquals("`", (geometry.spec(geometry.keyAt(1f, 1f)).def as KeyDef.Char).primary)
    }

    @Test
    fun `the bottom left corner is escape`() {
        assertEquals(KeyDef.Special(KeyCode.ESC), geometry.spec(geometry.keyAt(1f, 599f)).def)
    }

    @Test
    fun `each letter row splits between the hands within half a unit of the centre`() {
        val unit = 1080f / 15f
        for (firstRightHandKey in listOf("y", "h", "n")) {
            val index = indexOfChar(firstRightHandKey)
            assertEquals(firstRightHandKey, 540f, geometry.rects[index * 4], unit / 2f)
        }
    }

    @Test
    fun `the space bar is within a quarter unit of the centre`() {
        val space = indexOfChar(" ")
        val centre = (geometry.rects[space * 4] + geometry.rects[space * 4 + 2]) / 2f
        assertEquals(540f, centre, 1080f / 15f / 4f + 0.5f)
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
        assertEquals(2.25f * unit, geometry.rects[(firstOfRow3 + 1) * 4], 0.5f)
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

    private fun indexOfChar(primary: String): Int =
        (0 until geometry.keyCount).first {
            (geometry.spec(it).def as? KeyDef.Char)?.primary == primary
        }

    private fun centerX(code: KeyCode): Float {
        val index = indexOf(code)
        return (geometry.rects[index * 4] + geometry.rects[index * 4 + 2]) / 2f
    }

    private val inset =
        KeyboardGeometry(
            Layout(
                "t",
                "T",
                listOf(Row(List(9) { KeySpec(KeyDef.Char("$it", "$it"), "$it") }, inset = 0.5f)),
            ),
            widthPx = 1000,
            rowHeightPx = 100,
        )

    @Test
    fun `an inset row starts half a key in`() {
        assertEquals(50f, inset.rects[0], 0.01f)
        assertEquals(100f, inset.rects[2] - inset.rects[0], 0.01f)
    }

    @Test
    fun `a tap in the inset goes to the edge key`() {
        assertEquals(0, inset.keyAt(10f, 50f))
        assertEquals(8, inset.keyAt(995f, 50f))
    }
}
