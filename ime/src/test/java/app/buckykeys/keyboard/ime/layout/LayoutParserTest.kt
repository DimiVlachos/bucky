package app.buckykeys.keyboard.ime.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutParserTest {

    private fun layoutWithKeys(vararg keys: String): String =
        """{ "id": "test", "name": "Test", "rows": [ { "keys": [ ${keys.joinToString()} ] } ] }"""

    private fun parseKeys(vararg keys: String): List<KeySpec> =
        LayoutParser.parse(layoutWithKeys(*keys)).getOrThrow().rows.single().keys

    private fun failureOf(json: String): LayoutParseException {
        val error = LayoutParser.parse(json).exceptionOrNull()
        assertTrue("expected a LayoutParseException, got $error", error is LayoutParseException)
        return error as LayoutParseException
    }

    @Test
    fun `a layout keeps its id and name`() {
        val layout = LayoutParser.parse(layoutWithKeys("""{ "chars": ["q", "Q"] }""")).getOrThrow()

        assertEquals("test", layout.id)
        assertEquals("Test", layout.name)
    }

    @Test
    fun `a character key is labelled with its primary character`() {
        assertEquals(
            listOf(KeySpec(KeyDef.Char("q", "Q"), "q")),
            parseKeys("""{ "chars": ["q", "Q"] }"""),
        )
    }

    @Test
    fun `a single character takes its uppercase as the shifted character`() {
        assertEquals(KeyDef.Char("ü", "Ü"), parseKeys("""{ "chars": ["ü"] }""").single().def)
    }

    @Test
    fun `the space key has an empty label`() {
        assertEquals("", parseKeys("""{ "chars": [" ", " "] }""").single().label)
    }

    @Test
    fun `long press key names become special keys`() {
        assertEquals(
            KeyDef.Char("1", "!", listOf(KeyDef.Special(KeyCode.F1))),
            parseKeys("""{ "chars": ["1", "!"], "longPress": ["F1"] }""").single().def,
        )
    }

    @Test
    fun `other long press entries become characters with their uppercase`() {
        assertEquals(
            listOf(KeyDef.Char("é", "É"), KeyDef.Char("1", "1")),
            (parseKeys("""{ "chars": ["e"], "longPress": ["é", "1"] }""").single().def
                    as KeyDef.Char)
                .alternates,
        )
    }

    @Test
    fun `special keys and modifiers are named with key`() {
        assertEquals(
            listOf(KeyDef.Special(KeyCode.ESC), KeyDef.Modifier(ModifierKind.CTRL)),
            parseKeys("""{ "key": "ESC" }""", """{ "key": "CTRL" }""").map { it.def },
        )
    }

    @Test
    fun `special keys get their default label`() {
        assertEquals(
            listOf("Esc", "⌫", "Ctrl"),
            parseKeys("""{ "key": "ESC" }""", """{ "key": "BACKSPACE" }""", """{ "key": "CTRL" }""")
                .map { it.label },
        )
    }

    @Test
    fun `a label overrides the default`() {
        assertEquals("Escape", parseKeys("""{ "key": "ESC", "label": "Escape" }""").single().label)
    }

    @Test
    fun `width defaults to one unit`() {
        assertEquals(
            listOf(1f, 1.5f),
            parseKeys("""{ "key": "ESC" }""", """{ "key": "TAB", "width": 1.5 }""").map {
                it.width
            },
        )
    }

    @Test
    fun `switchTo makes a layout switch key`() {
        assertEquals(
            KeySpec(KeyDef.LayoutSwitch("numeric"), "123"),
            parseKeys("""{ "switchTo": "numeric", "label": "123" }""").single(),
        )
    }

    @Test
    fun `malformed json fails at the root`() {
        assertEquals("$", failureOf("""{ "id": """).path)
    }

    @Test
    fun `a missing field names its path`() {
        val error = failureOf("""{ "id": "test", "rows": [] }""")

        assertEquals("$.name", error.path)
        assertEquals("missing", error.reason)
    }

    @Test
    fun `an unknown key name points at the key`() {
        val error = failureOf(layoutWithKeys("""{ "chars": ["q"] }""", """{ "key": "ESCAPE" }"""))

        assertEquals("$.rows[0].keys[1].key", error.path)
        assertTrue(error.reason, error.reason.contains("ESCAPE"))
    }

    @Test
    fun `an unknown field is rejected so typos do not pass silently`() {
        assertEquals(
            "$.rows[0].keys[0].widht",
            failureOf(layoutWithKeys("""{ "key": "TAB", "widht": 1.5 }""")).path,
        )
    }

    @Test
    fun `a key needs exactly one of chars, key or switchTo`() {
        assertEquals(
            "$.rows[0].keys[0]",
            failureOf(layoutWithKeys("""{ "key": "TAB", "chars": ["q"] }""")).path,
        )
        assertEquals("$.rows[0].keys[0]", failureOf(layoutWithKeys("""{ "width": 2 }""")).path)
    }

    @Test
    fun `width must be positive`() {
        assertEquals(
            "$.rows[0].keys[0].width",
            failureOf(layoutWithKeys("""{ "key": "TAB", "width": 0 }""")).path,
        )
    }

    @Test
    fun `chars holds one or two non-empty strings`() {
        assertEquals(
            "$.rows[0].keys[0].chars",
            failureOf(layoutWithKeys("""{ "chars": [] }""")).path,
        )
        assertEquals(
            "$.rows[0].keys[0].chars",
            failureOf(layoutWithKeys("""{ "chars": ["a", "b", "c"] }""")).path,
        )
        assertEquals(
            "$.rows[0].keys[0].chars[0]",
            failureOf(layoutWithKeys("""{ "chars": [""] }""")).path,
        )
    }

    @Test
    fun `a layout needs at least one row and every row at least one key`() {
        assertEquals("$.rows", failureOf("""{ "id": "t", "name": "T", "rows": [] }""").path)
        assertEquals(
            "$.rows[0].keys",
            failureOf("""{ "id": "t", "name": "T", "rows": [ { "keys": [] } ] }""").path,
        )
    }

    @Test
    fun `a field of the wrong type names its path`() {
        assertEquals(
            "$.rows[0].keys[0].width",
            failureOf(layoutWithKeys("""{ "key": "TAB", "width": "wide" }""")).path,
        )
    }

    @Test
    fun `long press needs a character key`() {
        assertEquals(
            "$.rows[0].keys[0].longPress",
            failureOf(layoutWithKeys("""{ "key": "TAB", "longPress": ["x"] }""")).path,
        )
    }

    @Test
    fun `a width too large for a float is rejected`() {
        assertEquals(
            "$.rows[0].keys[0].width",
            failureOf(layoutWithKeys("""{ "key": "TAB", "width": 1e40 }""")).path,
        )
    }

    @Test
    fun `rows and keys must be objects and chars an array`() {
        assertEquals("$.rows[0]", failureOf("""{ "id": "t", "name": "T", "rows": [ 1 ] }""").path)
        assertEquals("$.rows[0].keys[0]", failureOf(layoutWithKeys("\"q\"")).path)
        assertEquals(
            "$.rows[0].keys[0].chars",
            failureOf(layoutWithKeys("""{ "chars": "q" }""")).path,
        )
    }
}
