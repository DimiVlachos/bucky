package app.buckykeys.keyboard.ime.view

import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.KeyCode
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.ModifierKind
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyStyleTest {

    @Test
    fun `special keys, modifiers and layout switches draw darker`() {
        assertEquals(true, isSpecialKey(KeyDef.Special(KeyCode.ESC)))
        assertEquals(true, isSpecialKey(KeyDef.Modifier(ModifierKind.CTRL)))
        assertEquals(true, isSpecialKey(KeyDef.LayoutSwitch(BuiltInLayouts.PAGE_SYMBOLS)))
    }

    @Test
    fun `characters, the space bar and dead keys keep the normal colour`() {
        assertEquals(false, isSpecialKey(KeyDef.Char("a", "A")))
        assertEquals(false, isSpecialKey(KeyDef.Char(" ", " ")))
        assertEquals(false, isSpecialKey(KeyDef.Dead("΄", "¨")))
    }
}
