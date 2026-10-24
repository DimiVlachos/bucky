package app.buckykeys.keyboard.ime.view

import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.KeySpec
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.ime.layout.Row
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageSetTest {

    private fun layout(id: String) =
        Layout(id, id, listOf(Row(listOf(KeySpec(KeyDef.Char("a", "A"), "a")))))

    private val english = layout("en")
    private val greek = layout("el")
    private val symbols = layout("symbols")
    private val pages =
        PageSet(listOf(english, greek), mapOf(BuiltInLayouts.PAGE_SYMBOLS to symbols))

    @Test
    fun `letters follow the language`() {
        assertEquals(english, pages.current(0, BuiltInLayouts.PAGE_LETTERS))
        assertEquals(greek, pages.current(1, BuiltInLayouts.PAGE_LETTERS))
    }

    @Test
    fun `a symbol page is shared across languages`() {
        assertEquals(symbols, pages.current(0, BuiltInLayouts.PAGE_SYMBOLS))
        assertEquals(symbols, pages.current(1, BuiltInLayouts.PAGE_SYMBOLS))
    }

    @Test
    fun `an unknown page falls back to letters`() {
        assertEquals(greek, pages.current(1, BuiltInLayouts.PAGE_SYMBOLS_MORE))
    }

    @Test
    fun `page targets are recognised and others are not`() {
        assertTrue(pages.isPage(BuiltInLayouts.PAGE_LETTERS))
        assertTrue(pages.isPage(BuiltInLayouts.PAGE_SYMBOLS))
        assertFalse(pages.isPage(BuiltInLayouts.PAGE_SYMBOLS_MORE))
        assertFalse(pages.isPage(BuiltInLayouts.NEXT_LANGUAGE))
    }

    @Test
    fun `no letters falls back to the built-in layout`() {
        assertEquals(1, PageSet(emptyList(), emptyMap()).languageCount)
    }
}
