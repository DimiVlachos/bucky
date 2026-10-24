package app.buckykeys.keyboard.ime.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtraRowsTest {

    private val base = FallbackLayout.pcFullEnUs

    @Test
    fun `no extra rows leaves the layout as it is`() {
        assertEquals(base, base.withExtraRows(fKeyRow = false, userKeys = emptyList()))
    }

    @Test
    fun `the f-key row is F1 to F12 on top and fills fifteen units`() {
        val top = base.withExtraRows(fKeyRow = true, userKeys = emptyList()).rows.first().keys
        assertEquals(
            KeyCode.entries.filter { it.name.matches(Regex("F\\d+")) }.map { KeyDef.Special(it) },
            top.map { it.def },
        )
        assertEquals(15f, top.sumOf { it.width.toDouble() }.toFloat(), 0.001f)
        assertEquals("F1", top.first().label)
    }

    @Test
    fun `the user row takes termux names, modifiers and single characters`() {
        val row =
            base
                .withExtraRows(
                    fKeyRow = false,
                    userKeys = listOf("ESC", "TAB", "CTRL", "~", "|", "PGUP", "BKSP", "DEL"),
                )
                .rows
                .first()
                .keys
        assertEquals(
            listOf(
                KeyDef.Special(KeyCode.ESC),
                KeyDef.Special(KeyCode.TAB),
                KeyDef.Modifier(ModifierKind.CTRL),
                KeyDef.Char("~", "~"),
                KeyDef.Char("|", "|"),
                KeyDef.Special(KeyCode.PAGE_UP),
                KeyDef.Special(KeyCode.BACKSPACE),
                KeyDef.Special(KeyCode.FORWARD_DEL),
            ),
            row.map { it.def },
        )
        assertEquals(
            listOf("Esc", "Tab", "Ctrl", "~", "|", "PgUp", "⌫", "Del"),
            row.map { it.label },
        )
    }

    @Test
    fun `user keys share the row equally`() {
        val row =
            base
                .withExtraRows(fKeyRow = false, userKeys = listOf("ESC", "/", "-"))
                .rows
                .first()
                .keys
        assertEquals(listOf(5f, 5f, 5f), row.map { it.width })
    }

    @Test
    fun `names are case insensitive`() {
        val row =
            base.withExtraRows(fKeyRow = false, userKeys = listOf("esc", "Ctrl")).rows.first().keys
        assertEquals(
            listOf(KeyDef.Special(KeyCode.ESC), KeyDef.Modifier(ModifierKind.CTRL)),
            row.map { it.def },
        )
    }

    @Test
    fun `words that are not key names are skipped`() {
        val row =
            base
                .withExtraRows(fKeyRow = false, userKeys = listOf("git", "ESC", ""))
                .rows
                .first()
                .keys
        assertEquals(listOf(KeyDef.Special(KeyCode.ESC)), row.map { it.def })
    }

    @Test
    fun `a user row with no valid keys adds nothing`() {
        assertEquals(base, base.withExtraRows(fKeyRow = false, userKeys = listOf("git", "status")))
    }

    @Test
    fun `the user row holds at most fifteen keys`() {
        val row = base.withExtraRows(fKeyRow = false, userKeys = List(20) { "/" }).rows.first().keys
        assertEquals(15, row.size)
    }

    @Test
    fun `the f-key row sits above the user row`() {
        val rows = base.withExtraRows(fKeyRow = true, userKeys = listOf("ESC")).rows
        assertEquals(KeyDef.Special(KeyCode.F1), rows[0].keys.first().def)
        assertEquals(KeyDef.Special(KeyCode.ESC), rows[1].keys.single().def)
        assertEquals(base.rows, rows.drop(2))
    }
}
