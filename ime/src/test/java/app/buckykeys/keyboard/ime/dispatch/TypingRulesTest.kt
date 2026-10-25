package app.buckykeys.keyboard.ime.dispatch

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TypingRulesTest {

    @Test
    fun `raw enter always sends the key`() {
        assertEquals(
            NO_ENTER_ACTION,
            enterAction(EditorInfo.IME_ACTION_SEND, rawEnter = true),
        )
    }

    @Test
    fun `a send field performs send`() {
        assertEquals(
            EditorInfo.IME_ACTION_SEND,
            enterAction(EditorInfo.IME_ACTION_SEND, rawEnter = false),
        )
    }

    @Test
    fun `a search field performs search`() {
        assertEquals(
            EditorInfo.IME_ACTION_SEARCH,
            enterAction(
                EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI,
                rawEnter = false,
            ),
        )
    }

    @Test
    fun `no enter action flag sends the key`() {
        val options = EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION
        assertEquals(NO_ENTER_ACTION, enterAction(options, rawEnter = false))
    }

    @Test
    fun `shift does not stop the enter action`() {
        assertEquals(
            EditorInfo.IME_ACTION_SEARCH,
            enterKeyAction(
                KeyEvent.META_SHIFT_ON,
                EditorInfo.IME_ACTION_SEARCH,
                rawEnter = false,
            ),
        )
    }

    @Test
    fun `ctrl alt or meta enter stays a raw key`() {
        listOf(KeyEvent.META_CTRL_ON, KeyEvent.META_ALT_ON, KeyEvent.META_META_ON).forEach { meta ->
            assertEquals(
                NO_ENTER_ACTION,
                enterKeyAction(meta, EditorInfo.IME_ACTION_SEARCH, rawEnter = false),
            )
        }
    }

    @Test
    fun `an unspecified action sends the key`() {
        assertEquals(
            NO_ENTER_ACTION,
            enterAction(EditorInfo.IME_ACTION_UNSPECIFIED, rawEnter = false),
        )
        assertEquals(
            NO_ENTER_ACTION,
            enterAction(EditorInfo.IME_ACTION_NONE, rawEnter = false),
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

    @Test
    fun `the period window is open only within a second`() {
        assertTrue(periodWindowOpen(300))
        assertFalse(periodWindowOpen(1500))
        assertFalse(periodWindowOpen(Long.MAX_VALUE))
    }
}
