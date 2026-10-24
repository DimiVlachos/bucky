package app.buckykeys.keyboard.settings.keyboard.presentation.state

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtraRowDraftStateTest {

    private val saved = listOf("ESC", "TAB")

    @Test
    fun `an untouched draft shows the saved row`() {
        assertEquals("ESC TAB", ExtraRowDraftState(null).display(saved))
    }

    @Test
    fun `an edited draft shows what was typed`() {
        val draft = ExtraRowDraftState(null)
        draft.edit("ESC TAB CTRL")
        assertEquals("ESC TAB CTRL", draft.display(saved))
    }

    @Test
    fun `an edited draft can be emptied`() {
        val draft = ExtraRowDraftState(null)
        draft.edit("")
        assertEquals("", draft.display(saved))
    }

    @Test
    fun `reset goes back to the saved row`() {
        val draft = ExtraRowDraftState(null)
        draft.edit("ESC")
        draft.reset()
        assertEquals("ESC TAB", draft.display(saved))
    }
}
