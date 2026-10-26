package app.buckykeys.keyboard.ime.layout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompactLayoutsTest {

    private fun load(id: String): Layout =
        LayoutParser.parse(File("src/main/assets/layouts/$id.json").readText()).getOrThrow()

    private val pages =
        listOf(
                BuiltInLayouts.COMPACT_EN_US,
                BuiltInLayouts.COMPACT_EL,
                BuiltInLayouts.COMPACT_ET,
                BuiltInLayouts.COMPACT_SYMBOLS,
                BuiltInLayouts.COMPACT_SYMBOLS_MORE,
            )
            .associateWith { load(it) }

    private val keyboardKey =
        KeyDef.LayoutSwitch(BuiltInLayouts.OTHER_KEYBOARD, BuiltInLayouts.FULL_LAYOUT)

    private fun Layout.keys(): List<KeySpec> = rows.flatMap { it.keys }

    private fun Layout.char(primary: String): KeyDef.Char =
        keys().map { it.def }.filterIsInstance<KeyDef.Char>().first { it.primary == primary }

    @Test
    fun `every compact page parses`() {
        assertEquals(5, pages.size)
    }

    @Test
    fun `every compact row spans ten units`() {
        pages.forEach { (id, layout) ->
            layout.rows.forEachIndexed { index, row ->
                val units = row.keys.sumOf { it.width.toDouble() } + 2 * row.inset
                assertEquals("$id row $index", 10.0, units, 0.001)
            }
        }
    }

    @Test
    fun `holding a top-row letter offers its digit`() {
        val english = pages.getValue(BuiltInLayouts.COMPACT_EN_US)
        assertEquals(KeyDef.Char("1", "1"), english.char("q").alternates.first())
        assertEquals(KeyDef.Char("0", "0"), english.char("p").alternates.first())
        val greek = pages.getValue(BuiltInLayouts.COMPACT_EL)
        assertEquals(KeyDef.Char("3", "3"), greek.char("ε").alternates.first())
    }

    @Test
    fun `english letters keep their accents`() {
        val alternates = pages.getValue(BuiltInLayouts.COMPACT_EN_US).char("a").alternates
        assertTrue(KeyDef.Char("ä", "Ä") in alternates)
    }

    @Test
    fun `the greek compact layout has the tonos dead key`() {
        val dead =
            pages
                .getValue(BuiltInLayouts.COMPACT_EL)
                .keys()
                .map { it.def }
                .filterIsInstance<KeyDef.Dead>()
        assertEquals(listOf("΄"), dead.map { it.primary })
    }

    @Test
    fun `every compact page can get back to letters or symbols`() {
        pages.forEach { (id, layout) ->
            val targets = layout.keys().mapNotNull { (it.def as? KeyDef.LayoutSwitch)?.targetId }
            assertTrue(id, targets.any { it.startsWith("@page:") })
        }
    }

    @Test
    fun `the keyboard key holds to the full layout on every page`() {
        pages.forEach { (id, layout) ->
            assertTrue(id, layout.keys().any { it.def == keyboardKey })
        }
    }

    @Test
    fun `a space on a symbol page returns to letters`() {
        listOf(BuiltInLayouts.COMPACT_SYMBOLS, BuiltInLayouts.COMPACT_SYMBOLS_MORE).forEach { id ->
            val space = pages.getValue(id).keys().first { it.def == KeyDef.Char(" ", " ") }
            assertEquals(id, BuiltInLayouts.PAGE_LETTERS, space.then)
        }
    }

    @Test
    fun `a space on the letters page stays on letters`() {
        val space =
            pages.getValue(BuiltInLayouts.COMPACT_EN_US).keys().first {
                it.def == KeyDef.Char(" ", " ")
            }
        assertEquals(null, space.then)
    }

    @Test
    fun `greek compact offers the ano teleia on the full stop`() {
        assertTrue(
            KeyDef.Char("·", "·") in pages.getValue(BuiltInLayouts.COMPACT_EL).char(".").alternates,
        )
    }

    @Test
    fun `the symbol page offers guillemets on the quote`() {
        val quote = pages.getValue(BuiltInLayouts.COMPACT_SYMBOLS).char("\"").alternates
        assertTrue(KeyDef.Char("«", "«") in quote && KeyDef.Char("»", "»") in quote)
    }

    @Test
    fun `the symbol page leads with the dollar and holds for the euro`() {
        assertEquals(
            KeyDef.Char("€", "€"),
            pages.getValue(BuiltInLayouts.COMPACT_SYMBOLS).char("$").alternates.first(),
        )
    }
}
