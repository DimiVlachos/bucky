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
}
