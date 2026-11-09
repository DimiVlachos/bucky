package app.buckykeys.keyboard.ime.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class TouchBiasTest {

    @Test
    fun `a new bias moves nothing`() {
        val bias = TouchBias()
        assertEquals(0f, bias.x, 0f)
        assertEquals(0f, bias.y, 0f)
    }

    @Test
    fun `repeated samples pull the bias toward their offset`() {
        val bias = TouchBias()
        repeat(200) { bias.learn(-0.2f, 0.1f) }
        assertEquals(-0.2f, bias.x, 0.001f)
        assertEquals(0.1f, bias.y, 0.001f)
    }

    @Test
    fun `one sample moves the bias only a little`() {
        val bias = TouchBias()
        bias.learn(0.3f, 0.3f)
        assertEquals(0.015f, bias.x, 0.001f)
    }

    @Test
    fun `the bias never moves a touch more than three tenths of a row`() {
        val bias = TouchBias()
        repeat(500) { bias.learn(5f, -5f) }
        assertEquals(TouchBias.MAX_ROWS, bias.x, 0f)
        assertEquals(-TouchBias.MAX_ROWS, bias.y, 0f)
    }
}
