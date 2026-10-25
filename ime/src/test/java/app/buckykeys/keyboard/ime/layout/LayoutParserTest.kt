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
            KeySpec(KeyDef.LayoutSwitch("@language"), "🌐"),
            parseKeys("""{ "switchTo": "@language", "label": "🌐" }""").single(),
        )
    }

    @Test
    fun `latin gives a character key the letter that ctrl uses`() {
        assertEquals(
            KeyDef.Char("ψ", "Ψ", latin = "c"),
            parseKeys("""{ "chars": ["ψ"], "latin": "c" }""").single().def,
        )
    }

    @Test
    fun `latin is a single character`() {
        assertEquals(
            "$.rows[0].keys[0].latin",
            failureOf(layoutWithKeys("""{ "chars": ["ψ"], "latin": "cc" }""")).path,
        )
    }

    @Test
    fun `a dead key holds its two marks and its latin key`() {
        assertEquals(
            KeySpec(KeyDef.Dead("΄", "¨", latin = ";"), "΄"),
            parseKeys("""{ "dead": ["΄", "¨"], "latin": ";" }""").single(),
        )
    }

    @Test
    fun `a dead key needs one or two marks`() {
        assertEquals("$.rows[0].keys[0].dead", failureOf(layoutWithKeys("""{ "dead": [] }""")).path)
    }

    @Test
    fun `a layout with dead keys precomputes what each letter composes into`() {
        val keys =
            parseKeys(
                """{ "dead": ["΄", "¨"] }""",
                """{ "chars": ["α", "Α"] }""",
                """{ "chars": ["ι", "Ι"] }""",
                """{ "chars": ["ψ", "Ψ"] }""",
            )
        val alpha = keys[1].def as KeyDef.Char
        val iota = keys[2].def as KeyDef.Char
        val psi = keys[3].def as KeyDef.Char
        assertEquals(mapOf("΄" to "ά"), alpha.composed)
        assertEquals(mapOf("΄" to "Ά"), alpha.composedShifted)
        assertEquals(mapOf("΄" to "ί", "¨" to "ϊ"), iota.composed)
        assertEquals(emptyMap<String, String>(), psi.composed)
    }

    @Test
    fun `a layout without dead keys composes nothing`() {
        assertEquals(
            emptyMap<String, String>(),
            (parseKeys("""{ "chars": ["e"] }""").single().def as KeyDef.Char).composed,
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

    @Test
    fun `a switch target other than the next language is rejected`() {
        val error = failureOf(layoutWithKeys("""{ "switchTo": "numeric" }"""))

        assertEquals("$.rows[0].keys[0].switchTo", error.path)
        assertTrue(error.reason, error.reason.startsWith("unsupported switch target"))
    }

    @Test
    fun `a long press entry that looks like an unknown key name is rejected`() {
        listOf("ESCAPE", "CTRL", "f1", "esc").forEach { name ->
            val error =
                failureOf(layoutWithKeys("""{ "chars": ["e"], "longPress": ["é", "$name"] }"""))
            assertEquals(name, "$.rows[0].keys[0].longPress[1]", error.path)
        }
    }

    @Test
    fun `long press keeps single characters and non-ascii words as text`() {
        assertEquals(
            listOf(KeyDef.Char("Q", "Q"), KeyDef.Char("ου", "ΟΥ")),
            (parseKeys("""{ "chars": ["o"], "longPress": ["Q", "ου"] }""").single().def
                    as KeyDef.Char)
                .alternates,
        )
    }

    @Test
    fun `latin must be a key the us layout has`() {
        listOf("é", "ψ", "!", " ").forEach { latin ->
            assertEquals(
                latin,
                "$.rows[0].keys[0].latin",
                failureOf(layoutWithKeys("""{ "chars": ["ψ"], "latin": "$latin" }""")).path,
            )
        }
        assertEquals(
            listOf("c", "7", ";", "/"),
            listOf("c", "7", ";", "/").map { latin ->
                (parseKeys("""{ "chars": ["ψ"], "latin": "$latin" }""").single().def as KeyDef.Char)
                    .latin
            },
        )
    }

    @Test
    fun `a dead mark that composes with nothing is rejected`() {
        val error = failureOf(layoutWithKeys("""{ "dead": ["΄", "'"] }"""))

        assertEquals("$.rows[0].keys[0].dead[1]", error.path)
        assertEquals("dead mark has no combining form", error.reason)
    }

    @Test
    fun `the fn modifier parses`() {
        assertEquals(
            KeySpec(KeyDef.Modifier(ModifierKind.FN), "Fn"),
            parseKeys("""{ "key": "FN" }""").single(),
        )
    }

    @Test
    fun `a key can name its fn variant`() {
        assertEquals(
            KeySpec(KeyDef.Special(KeyCode.UP), "↑", fn = KeyDef.Special(KeyCode.PAGE_UP)),
            parseKeys("""{ "key": "UP", "fn": "PAGE_UP" }""").single(),
        )
    }

    @Test
    fun `an unknown fn variant is rejected`() {
        val error = failureOf(layoutWithKeys("""{ "key": "UP", "fn": "PAGE_SIDEWAYS" }"""))
        assertEquals("$.rows[0].keys[0].fn", error.path)
    }

    @Test
    fun `the language name defaults to the layout name`() {
        assertEquals(
            "Test",
            LayoutParser.parse(layoutWithKeys("""{ "chars": ["a"] }""")).getOrThrow().language,
        )
    }

    @Test
    fun `a layout can name its language`() {
        val json =
            """{ "id": "t", "name": "PC Full (US)", "language": "English (US)", "rows": [ { "keys": [ { "chars": ["a"] } ] } ] }"""
        assertEquals("English (US)", LayoutParser.parse(json).getOrThrow().language)
    }

    @Test
    fun `a row can have an inset`() {
        val json =
            """{ "id": "t", "name": "T", "rows": [ { "inset": 0.5, "keys": [ { "chars": ["a"] } ] } ] }"""
        assertEquals(0.5f, LayoutParser.parse(json).getOrThrow().rows.single().inset, 0.001f)
    }

    @Test
    fun `a negative inset is rejected`() {
        val error =
            failureOf(
                """{ "id": "t", "name": "T", "rows": [ { "inset": -1, "keys": [ { "chars": ["a"] } ] } ] }""",
            )
        assertEquals("$.rows[0].inset", error.path)
    }

    @Test
    fun `a key can name a switch to follow it`() {
        val key = parseKeys("""{ "chars": [" "], "then": "@page:letters" }""").single()
        assertEquals(BuiltInLayouts.PAGE_LETTERS, key.then)
    }

    @Test
    fun `a switch key can name a hold target`() {
        val key =
            parseKeys("""{ "switchTo": "@keyboard", "holdTo": "@full", "label": "⌨" }""").single()
        assertEquals(
            KeyDef.LayoutSwitch(BuiltInLayouts.OTHER_KEYBOARD, BuiltInLayouts.FULL_LAYOUT),
            key.def,
        )
    }

    @Test
    fun `page targets parse`() {
        listOf(
                BuiltInLayouts.PAGE_LETTERS,
                BuiltInLayouts.PAGE_SYMBOLS,
                BuiltInLayouts.PAGE_SYMBOLS_MORE,
            )
            .forEach { target ->
                val key = parseKeys("""{ "switchTo": "$target" }""").single()
                assertEquals(KeyDef.LayoutSwitch(target), key.def)
            }
    }

    @Test
    fun `page switches get their phone keyboard labels`() {
        assertEquals("?123", parseKeys("""{ "switchTo": "@page:symbols" }""").single().label)
        assertEquals("ABC", parseKeys("""{ "switchTo": "@page:letters" }""").single().label)
        assertEquals("=\\<", parseKeys("""{ "switchTo": "@page:symbols-more" }""").single().label)
        assertEquals("⌨", parseKeys("""{ "switchTo": "@keyboard" }""").single().label)
    }

    @Test
    fun `an unknown page target is rejected`() {
        val error = failureOf(layoutWithKeys("""{ "switchTo": "@page:emoji" }"""))
        assertEquals("$.rows[0].keys[0].switchTo", error.path)
    }

    @Test
    fun `holdTo only accepts the full layout`() {
        val error =
            failureOf(layoutWithKeys("""{ "switchTo": "@keyboard", "holdTo": "@language" }"""))
        assertEquals("$.rows[0].keys[0].holdTo", error.path)
    }

    @Test
    fun `a row keeps its inset when the layout has a dead key`() {
        val json =
            """{ "id": "t", "name": "T", "rows": [ { "inset": 0.5, "keys": [ { "dead": ["΄"] }, { "chars": ["α"] } ] } ] }"""
        assertEquals(0.5f, LayoutParser.parse(json).getOrThrow().rows.single().inset, 0.001f)
    }

    @Test
    fun `then is rejected on keys that never type`() {
        val error = failureOf(layoutWithKeys("""{ "key": "SHIFT", "then": "@page:letters" }"""))
        assertEquals("$.rows[0].keys[0].then", error.path)
    }

    @Test
    fun `fn is rejected on modifier keys`() {
        val error = failureOf(layoutWithKeys("""{ "key": "SHIFT", "fn": "F1" }"""))
        assertEquals("$.rows[0].keys[0].fn", error.path)
    }

    @Test
    fun `long press is rejected on the space bar`() {
        val error = failureOf(layoutWithKeys("""{ "chars": [" "], "longPress": ["_"] }"""))
        assertEquals("$.rows[0].keys[0].longPress", error.path)
    }

    @Test
    fun `a language key gets the globe label by default`() {
        assertEquals("🌐", parseKeys("""{ "switchTo": "@language" }""").single().label)
    }
}
