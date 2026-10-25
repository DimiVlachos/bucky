package app.buckykeys.keyboard.ime.view

import org.junit.Assert.assertEquals
import org.junit.Test

class LabelFitTest {

    @Test
    fun `a label that fits keeps the base size`() {
        assertEquals(
            36f,
            fittedTextSize(36f, labelWidth = 80f, availableWidth = 100f, minSize = 24f),
        )
    }

    @Test
    fun `a wider label shrinks in proportion to the overflow`() {
        assertEquals(
            30f,
            fittedTextSize(36f, labelWidth = 120f, availableWidth = 100f, minSize = 24f),
        )
    }

    @Test
    fun `shrinking stops at the minimum size`() {
        assertEquals(
            24f,
            fittedTextSize(36f, labelWidth = 400f, availableWidth = 100f, minSize = 24f),
        )
    }

    @Test
    fun `a key with no room uses the minimum size`() {
        assertEquals(24f, fittedTextSize(36f, labelWidth = 50f, availableWidth = 0f, minSize = 24f))
    }

    @Test
    fun `a label that fits its width still shrinks to the height cap`() {
        assertEquals(
            20f,
            fittedTextSize(
                36f,
                labelWidth = 50f,
                availableWidth = 100f,
                minSize = 24f,
                maxSize = 20f,
            ),
        )
    }

    @Test
    fun `the height cap wins over the minimum size`() {
        assertEquals(
            10f,
            fittedTextSize(
                36f,
                labelWidth = 400f,
                availableWidth = 100f,
                minSize = 24f,
                maxSize = 10f,
            ),
        )
    }

    @Test
    fun `a single latin letter is one visible character`() {
        assertEquals(1, visibleCharCount("a"))
    }

    @Test
    fun `an emoji outside the basic plane is one visible character`() {
        assertEquals(1, visibleCharCount("🌐"))
    }

    @Test
    fun `a letter with combining marks is one visible character`() {
        assertEquals(1, visibleCharCount("\u03A5\u0308\u0301"))
    }

    @Test
    fun `a word counts each of its letters`() {
        assertEquals(4, visibleCharCount("Ctrl"))
    }

    @Test
    fun `an empty label has no visible characters`() {
        assertEquals(0, visibleCharCount(""))
    }

    @Test
    fun `single character labels share the smallest size and word labels keep their own`() {
        val sizes = floatArrayOf(40f, 36f, 20f, 30f)
        val small = booleanArrayOf(false, false, true, true)
        evenBigLabels(sizes, small)
        assertEquals(listOf(36f, 36f, 20f, 30f), sizes.toList())
    }
}
