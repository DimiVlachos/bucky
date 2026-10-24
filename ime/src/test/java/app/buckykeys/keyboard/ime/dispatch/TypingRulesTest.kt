package app.buckykeys.keyboard.ime.dispatch

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TypingRulesTest {

    private val text = InputType.TYPE_CLASS_TEXT

    @Test
    fun `raw enter always sends the key`() {
        assertEquals(
            NO_ENTER_ACTION,
            enterAction(EditorInfo.IME_ACTION_SEND, text, rawEnter = true),
        )
    }

    @Test
    fun `a send field performs send`() {
        assertEquals(
            EditorInfo.IME_ACTION_SEND,
            enterAction(EditorInfo.IME_ACTION_SEND, text, rawEnter = false),
        )
    }

    @Test
    fun `a search field performs search`() {
        assertEquals(
            EditorInfo.IME_ACTION_SEARCH,
            enterAction(
                EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI,
                text,
                rawEnter = false,
            ),
        )
    }

    @Test
    fun `no enter action flag sends the key`() {
        val options = EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION
        assertEquals(NO_ENTER_ACTION, enterAction(options, text, rawEnter = false))
    }

    @Test
    fun `a multi line field sends the key`() {
        val multiLine = text or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        assertEquals(
            NO_ENTER_ACTION,
            enterAction(EditorInfo.IME_ACTION_SEND, multiLine, rawEnter = false),
        )
    }

    @Test
    fun `an unspecified action sends the key`() {
        assertEquals(
            NO_ENTER_ACTION,
            enterAction(EditorInfo.IME_ACTION_UNSPECIFIED, text, rawEnter = false),
        )
        assertEquals(
            NO_ENTER_ACTION,
            enterAction(EditorInfo.IME_ACTION_NONE, text, rawEnter = false),
        )
    }

    @Test
    fun `a letter and a space then a quick space inserts a period`() {
        assertTrue(shouldInsertPeriod("hi ", 300))
        assertTrue(shouldInsertPeriod("7 ", 300))
    }

    @Test
    fun `a slow second space does not`() {
        assertFalse(shouldInsertPeriod("hi ", 1500))
    }

    @Test
    fun `two spaces in a row do not`() {
        assertFalse(shouldInsertPeriod("  ", 300))
        assertFalse(shouldInsertPeriod(". ", 300))
    }

    @Test
    fun `nothing before the cursor does not`() {
        assertFalse(shouldInsertPeriod(null, 300))
        assertFalse(shouldInsertPeriod("", 300))
        assertFalse(shouldInsertPeriod(" ", 300))
    }
}
