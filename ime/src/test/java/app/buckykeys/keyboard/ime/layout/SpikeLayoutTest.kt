package app.buckykeys.keyboard.ime.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpikeLayoutTest {

    private val layout = SpikeLayout.pcFullEnUs

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
}
