package app.buckykeys.keyboard.settings.keyboard.presentation.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtraRowDraftStateTest {

    private fun draft() = ExtraRowDraftState(TextFieldState())

    @Test
    fun `syncing shows the saved row with the cursor at the end`() {
        val draft = draft()
        draft.syncTo("ESC TAB")
        assertEquals("ESC TAB", draft.text)
        assertEquals(TextRange(7), draft.textField.selection)
    }

    @Test
    fun `syncing the same saved row again keeps an unsaved edit`() {
        val draft = draft()
        draft.syncTo("ESC")
        draft.textField.setTextAndPlaceCursorAtEnd("ESC TAB")
        draft.syncTo("ESC")
        assertEquals("ESC TAB", draft.text)
    }

    @Test
    fun `a newly saved row replaces the field`() {
        val draft = draft()
        draft.syncTo("ESC")
        draft.syncTo("ESC TAB")
        assertEquals("ESC TAB", draft.text)
    }

    @Test
    fun `filling puts the cursor at the end`() {
        val draft = draft()
        draft.fill("ESC / -")
        assertEquals("ESC / -", draft.text)
        assertEquals(TextRange(7), draft.textField.selection)
    }
}
