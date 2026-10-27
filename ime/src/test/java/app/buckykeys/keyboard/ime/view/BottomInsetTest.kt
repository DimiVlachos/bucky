package app.buckykeys.keyboard.ime.view

import org.junit.Assert.assertEquals
import org.junit.Test

class BottomInsetTest {

    @Test
    fun `a window that runs under the nav bar pads by the bar`() {
        assertEquals(
            126,
            keysBottomPadding(barBottom = 126, gestureBottom = 126, reservedBelow = 0),
        )
    }

    @Test
    fun `a window that already keeps the nav bar clear adds no padding`() {
        assertEquals(
            0,
            keysBottomPadding(barBottom = 126, gestureBottom = 126, reservedBelow = 126),
        )
    }

    @Test
    fun `a side nav bar leaves no bottom padding`() {
        assertEquals(0, keysBottomPadding(barBottom = 0, gestureBottom = 0, reservedBelow = 0))
    }

    @Test
    fun `gesture nav pads by the larger of the handle and the gesture area`() {
        assertEquals(63, keysBottomPadding(barBottom = 42, gestureBottom = 63, reservedBelow = 0))
    }

    @Test
    fun `only the part the window does not reserve is padded`() {
        assertEquals(21, keysBottomPadding(barBottom = 42, gestureBottom = 63, reservedBelow = 42))
    }
}
