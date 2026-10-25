package app.buckykeys.keyboard.ime.view

import org.junit.Assert.assertEquals
import org.junit.Test

class TouchMappingTest {

    @Test
    fun `a touch in the edge band maps to the edge key`() {
        assertEquals(0f, edgeClamp(-3f, max = 999f, band = 8f), 0f)
        assertEquals(999f, edgeClamp(1004f, max = 999f, band = 8f), 0f)
    }

    @Test
    fun `a touch beyond the edge band stays outside so the key cancels`() {
        assertEquals(-40f, edgeClamp(-40f, max = 999f, band = 8f), 0f)
        assertEquals(1100f, edgeClamp(1100f, max = 999f, band = 8f), 0f)
    }

    @Test
    fun `a touch on the keys is unchanged`() {
        assertEquals(500f, edgeClamp(500f, max = 999f, band = 8f), 0f)
    }
}
