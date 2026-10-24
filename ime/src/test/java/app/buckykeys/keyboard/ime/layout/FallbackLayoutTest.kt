package app.buckykeys.keyboard.ime.layout

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackLayoutTest {

    private val layout = FallbackLayout.pcFullEnUs

    @Test
    fun `every row spans fifteen units`() {
        layout.rows.forEach { row ->
            assertEquals(15f, row.keys.sumOf { it.width.toDouble() }.toFloat(), 0.001f)
        }
    }

    @Test
    fun `backtick and tilde can be typed`() {
        assertTrue(layout.rows.flatMap { it.keys }.any { it.def == KeyDef.Char("`", "~") })
    }

    @Test
    fun `backslash and pipe can be typed`() {
        assertTrue(layout.rows.flatMap { it.keys }.any { it.def == KeyDef.Char("\\", "|") })
    }

    @Test
    fun `home and end are wide enough for their labels`() {
        val keys = layout.rows.flatMap { it.keys }
        listOf(KeyCode.HOME, KeyCode.END).forEach { code ->
            assertEquals(1.5f, keys.first { it.def == KeyDef.Special(code) }.width, 0.001f)
        }
    }

    @Test
    fun `keys with word labels are at least one and a quarter units wide`() {
        layout.rows
            .flatMap { it.keys }
            .filter { it.label.length > 1 }
            .forEach { key ->
                assertTrue("${key.label} is ${key.width}", key.width >= 1.25f)
            }
    }

    @Test
    fun `the four arrow keys are the same width`() {
        val keys = layout.rows.flatMap { it.keys }
        val widths =
            listOf(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT).map { code ->
                keys.first { it.def == KeyDef.Special(code) }.width
            }
        assertEquals(listOf(1.25f, 1.25f, 1.25f, 1.25f), widths)
    }

    @Test
    fun `the space bar is within half a key of the centre`() {
        val bottom = layout.rows.last().keys
        val spaceIndex = bottom.indexOfFirst { it.def == KeyDef.Char(" ", " ") }
        val left = bottom.take(spaceIndex).sumOf { it.width.toDouble() }
        val right = bottom.drop(spaceIndex + 1).sumOf { it.width.toDouble() }
        assertTrue("left $left, right $right", abs(left - right) <= 1.0)
    }

    @Test
    fun `the built-in layout has no language key`() {
        assertTrue(layout.rows.flatMap { it.keys }.none { it.def is KeyDef.LayoutSwitch })
    }
}
