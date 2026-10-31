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
        evenBigLabels(sizes, small, BooleanArray(4))
        assertEquals(listOf(36f, 36f, 20f, 30f), sizes.toList())
    }

    @Test
    fun `a narrow key keeps its own smaller fit without shrinking the other single characters`() {
        val sizes = floatArrayOf(40f, 36f, 24f)
        val narrow = booleanArrayOf(false, false, true)
        evenBigLabels(sizes, BooleanArray(3), narrow)
        assertEquals(listOf(36f, 36f, 24f), sizes.toList())
    }

    @Test
    fun `a narrow key never grows past the shared size`() {
        val sizes = floatArrayOf(36f, 40f)
        evenBigLabels(sizes, BooleanArray(2), booleanArrayOf(false, true))
        assertEquals(listOf(36f, 36f), sizes.toList())
    }

    @Test
    fun `a larger label size grows a fitted single character past its key`() {
        assertEquals(60f, scaledLabelSize(fittedSize = 30f, scale = 2f, maxSize = 100f), 0.001f)
    }

    @Test
    fun `a scaled label never grows past the row height cap`() {
        assertEquals(50f, scaledLabelSize(fittedSize = 30f, scale = 2f, maxSize = 50f), 0.001f)
    }

    @Test
    fun `a smaller label size shrinks the fitted size`() {
        assertEquals(24f, scaledLabelSize(fittedSize = 30f, scale = 0.8f, maxSize = 100f), 0.001f)
    }

    @Test
    fun `a single character keeps the key width in the preview`() {
        assertEquals(
            60f,
            previewBubbleWidth(
                keyWidth = 60f,
                rowHeight = 50f,
                paddedLabelWidth = 30f,
                keyboardWidth = 1000f,
            ),
            0.001f,
        )
    }

    @Test
    fun `a word label widens the preview to fit`() {
        assertEquals(
            130f,
            previewBubbleWidth(
                keyWidth = 60f,
                rowHeight = 50f,
                paddedLabelWidth = 130f,
                keyboardWidth = 1000f,
            ),
            0.001f,
        )
    }

    @Test
    fun `a word label widens the preview to at most three rows`() {
        assertEquals(
            150f,
            previewBubbleWidth(
                keyWidth = 60f,
                rowHeight = 50f,
                paddedLabelWidth = 400f,
                keyboardWidth = 1000f,
            ),
            0.001f,
        )
    }

    @Test
    fun `the preview never gets wider than the keyboard`() {
        assertEquals(
            120f,
            previewBubbleWidth(
                keyWidth = 60f,
                rowHeight = 50f,
                paddedLabelWidth = 400f,
                keyboardWidth = 120f,
            ),
            0.001f,
        )
    }

    @Test
    fun `a wide key keeps the two row preview cap`() {
        assertEquals(
            100f,
            previewBubbleWidth(
                keyWidth = 300f,
                rowHeight = 50f,
                paddedLabelWidth = 30f,
                keyboardWidth = 1000f,
            ),
            0.001f,
        )
    }
}
