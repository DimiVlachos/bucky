package app.buckykeys.keyboard.settings.keyboard.presentation.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtraRowDraftStateTest {

    private val saved = listOf("ESC", "TAB")

    @Test
    fun `an untouched draft shows the saved row and is unchanged`() {
        val draft = ExtraRowDraftState(null)
        assertEquals("ESC TAB", draft.display(saved))
        assertFalse(draft.changed(saved))
    }

    @Test
    fun `editing to different words is a change`() {
        val draft = ExtraRowDraftState(null)
        draft.edit("ESC TAB CTRL")
        assertTrue(draft.changed(saved))
        assertEquals("ESC TAB CTRL", draft.display(saved))
    }

    @Test
    fun `extra spaces alone are not a change`() {
        val draft = ExtraRowDraftState(null)
        draft.edit("  ESC   TAB ")
        assertFalse(draft.changed(saved))
    }

    @Test
    fun `reset goes back to the saved row`() {
        val draft = ExtraRowDraftState(null)
        draft.edit("ESC")
        draft.reset()
        assertEquals("ESC TAB", draft.display(saved))
        assertFalse(draft.changed(saved))
    }
}
