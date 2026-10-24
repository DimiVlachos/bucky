package app.buckykeys.keyboard.ime.view

import app.buckykeys.keyboard.ime.layout.KeyCode
import app.buckykeys.keyboard.ime.layout.KeyDef
import org.junit.Assert.assertEquals
import org.junit.Test

class HintsTest {

    @Test
    fun `a letter that holds for a digit shows the digit`() {
        assertEquals("1", hintFor(KeyDef.Char("q", "Q", listOf(KeyDef.Char("1", "1")))))
    }

    @Test
    fun `a letter that holds for its capital shows nothing`() {
        assertEquals(
            "",
            hintFor(KeyDef.Char("e", "E", listOf(KeyDef.Char("E", "E"), KeyDef.Char("é", "É")))),
        )
    }

    @Test
    fun `a symbol shows its first alternate`() {
        assertEquals("…", hintFor(KeyDef.Char(".", ">", listOf(KeyDef.Char("…", "…")))))
        assertEquals("F1", hintFor(KeyDef.Char("1", "!", listOf(KeyDef.Special(KeyCode.F1)))))
    }

    @Test
    fun `a key without alternates shows nothing`() {
        assertEquals("", hintFor(KeyDef.Char("w", "W")))
        assertEquals("", hintFor(KeyDef.Special(KeyCode.ESC)))
    }
}
