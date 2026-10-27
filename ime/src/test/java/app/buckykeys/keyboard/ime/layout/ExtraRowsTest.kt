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
            listOf("Esc", "⇥", "Ctrl", "~", "|", "PgUp", "⌫", "Del"),
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

    private fun userRow(vararg keys: String): List<KeySpec> =
        base.withExtraRows(fKeyRow = false, userKeys = keys.toList()).rows.first().keys

    @Test
    fun `termux names and aliases map to their keys`() {
        assertEquals(
            listOf(
                KeyDef.Char(" ", " "),
                KeyDef.Special(KeyCode.ESC),
                KeyDef.Modifier(ModifierKind.CTRL),
                KeyDef.Special(KeyCode.ENTER),
                KeyDef.Special(KeyCode.ENTER),
                KeyDef.Special(KeyCode.FORWARD_DEL),
                KeyDef.Special(KeyCode.PAGE_UP),
                KeyDef.Special(KeyCode.PAGE_UP),
                KeyDef.Special(KeyCode.PAGE_DOWN),
                KeyDef.Special(KeyCode.PAGE_DOWN),
                KeyDef.Char("\\", "\\"),
                KeyDef.Char("\"", "\""),
                KeyDef.Char("'", "'"),
                KeyDef.Special(KeyCode.INSERT),
                KeyDef.Special(KeyCode.UP),
            ),
            userRow(
                    "SPACE",
                    "escape",
                    "Control",
                    "RETURN",
                    "ENTER",
                    "DELETE",
                    "PAGEUP",
                    "PAGE_UP",
                    "PAGEDOWN",
                    "PGDOWN",
                    "BACKSLASH",
                    "QUOTE",
                    "APOSTROPHE",
                    "INS",
                    "up",
                )
                .map { it.def },
        )
        assertEquals(
            listOf(
                KeyDef.Special(KeyCode.DOWN),
                KeyDef.Special(KeyCode.LEFT),
                KeyDef.Special(KeyCode.RIGHT),
            ),
            userRow("DOWN", "LEFT", "RIGHT").map { it.def },
        )
    }

    @Test
    fun `the space key gets a visible label`() {
        assertEquals("␣", userRow("SPACE").single().label)
    }

    @Test
    fun `a pasted termux extra-keys line becomes one row in order`() {
        val pasted =
            "[['ESC','/','-','HOME','UP','END','PGUP'],['TAB','CTRL','ALT','LEFT','DOWN','RIGHT','PGDN']]"
        assertEquals(
            listOf(
                KeyDef.Special(KeyCode.ESC),
                KeyDef.Char("/", "/"),
                KeyDef.Char("-", "-"),
                KeyDef.Special(KeyCode.HOME),
                KeyDef.Special(KeyCode.UP),
                KeyDef.Special(KeyCode.END),
                KeyDef.Special(KeyCode.PAGE_UP),
                KeyDef.Special(KeyCode.TAB),
                KeyDef.Modifier(ModifierKind.CTRL),
                KeyDef.Modifier(ModifierKind.ALT),
                KeyDef.Special(KeyCode.LEFT),
                KeyDef.Special(KeyCode.DOWN),
                KeyDef.Special(KeyCode.RIGHT),
                KeyDef.Special(KeyCode.PAGE_DOWN),
            ),
            userRow(pasted).map { it.def },
        )
    }

    @Test
    fun `a termux line split on spaces after commas still parses`() {
        assertEquals(
            listOf(KeyDef.Special(KeyCode.ESC), KeyDef.Char(",", ","), KeyDef.Special(KeyCode.TAB)),
            userRow("[['ESC',", "\",\",", "'TAB']]").map { it.def },
        )
    }

    @Test
    fun `brackets and quotes typed alone stay characters`() {
        assertEquals(
            listOf(KeyDef.Char("[", "["), KeyDef.Char("'", "'"), KeyDef.Char(",", ",")),
            userRow("[", "'", ",").map { it.def },
        )
    }

    @Test
    fun `a character built from a letter and a combining accent counts as one key`() {
        assertEquals(KeyDef.Char("e\u0301", "E\u0301"), userRow("e\u0301").single().def)
    }

    @Test
    fun `a single letter shifts to its uppercase`() {
        assertEquals(
            listOf(KeyDef.Char("a", "A"), KeyDef.Char("ß", "ß")),
            userRow("a", "ß").map { it.def },
        )
    }

    @Test
    fun `the fn modifier can go in the row`() {
        assertEquals(
            listOf(KeyDef.Modifier(ModifierKind.FN), KeyDef.Special(KeyCode.ESC)),
            userRow("FN", "ESC").map { it.def },
        )
    }

    @Test
    fun `the fifteen key cap counts only valid keys`() {
        val keys = List(10) { "git" } + List(15) { "ESC" } + listOf("TAB")
        val row = userRow(*keys.toTypedArray())
        assertEquals(15, row.size)
        assertEquals(List(15) { KeyDef.Special(KeyCode.ESC) }, row.map { it.def })
    }

    @Test
    fun `parsing reports the keys it accepts with their labels`() {
        val parsed = parseExtraRow(listOf("ESC", "ctrl", "~", "SPACE"))
        assertEquals(listOf("Esc", "Ctrl", "~", "␣"), parsed.keys.map { it.label })
        assertEquals(emptyList<String>(), parsed.unknown)
        assertEquals(false, parsed.overLimit)
    }

    @Test
    fun `parsing reports words that are not keys`() {
        val parsed = parseExtraRow(listOf("git", "ESC", "status", "FN"))
        assertEquals(listOf("Esc", "Fn"), parsed.keys.map { it.label })
        assertEquals(listOf("git", "status"), parsed.unknown)
    }

    @Test
    fun `parsing reports more than fifteen keys`() {
        val parsed = parseExtraRow(List(16) { "/" })
        assertEquals(15, parsed.keys.size)
        assertEquals(true, parsed.overLimit)
    }

    @Test
    fun `parsing reads a pasted termux line`() {
        val parsed = parseExtraRow(listOf("[['ESC','/','-'],['TAB','CTRL']]"))
        assertEquals(listOf("Esc", "/", "-", "⇥", "Ctrl"), parsed.keys.map { it.label })
        assertEquals(emptyList<String>(), parsed.unknown)
    }

    @Test
    fun `the user row takes meta and the new specials by name`() {
        val parsed = parseExtraRow(listOf("META SYSRQ SCROLL_LOCK BREAK NUM_LOCK DPAD_CENTER"))
        assertEquals(emptyList<String>(), parsed.unknown)
        assertEquals(
            listOf(
                KeyDef.Modifier(ModifierKind.META),
                KeyDef.Special(KeyCode.SYSRQ),
                KeyDef.Special(KeyCode.SCROLL_LOCK),
                KeyDef.Special(KeyCode.BREAK),
                KeyDef.Special(KeyCode.NUM_LOCK),
                KeyDef.Special(KeyCode.DPAD_CENTER),
            ),
            parsed.keys.map { it.def },
        )
    }
}
