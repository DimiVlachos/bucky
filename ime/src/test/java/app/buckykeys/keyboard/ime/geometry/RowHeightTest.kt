package app.buckykeys.keyboard.ime.geometry

import org.junit.Assert.assertEquals
import org.junit.Test

class RowHeightTest {

    @Test
    fun `a keyboard that fits keeps the chosen row height`() {
        assertEquals(
            100,
            cappedRowHeightPx(100, rows = 5, screenHeightPx = 2400, landscape = false),
        )
    }

    @Test
    fun `seven portrait rows are capped at sixty percent of the screen`() {
        assertEquals(
            205,
            cappedRowHeightPx(300, rows = 7, screenHeightPx = 2400, landscape = false),
        )
    }

    @Test
    fun `seven landscape rows are capped at half the screen`() {
        assertEquals(77, cappedRowHeightPx(102, rows = 7, screenHeightPx = 1080, landscape = true))
    }

    @Test
    fun `an unknown screen height keeps the chosen row height`() {
        assertEquals(100, cappedRowHeightPx(100, rows = 7, screenHeightPx = 0, landscape = true))
    }

    @Test
    fun `the cap never drops below one pixel`() {
        assertEquals(1, cappedRowHeightPx(100, rows = 7, screenHeightPx = 3, landscape = true))
    }
}
