package app.buckykeys.keyboard.ime.layout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassicLayoutsTest {

    private fun load(id: String): Layout =
        LayoutParser.parse(File("src/main/assets/layouts/$id.json").readText()).getOrThrow()

    private val english = load(BuiltInLayouts.PC_HK_EN_US)
    private val greek = load(BuiltInLayouts.PC_HK_EL)
    private val estonian = load(BuiltInLayouts.PC_HK_ET)
    private val finnish = load(BuiltInLayouts.PC_HK_FI)
    private val fnPage = load(BuiltInLayouts.PC_HK_FN)

    private fun Layout.widths(): List<List<Float>> = rows.map { row -> row.keys.map { it.width } }

    private fun Row.startOf(matches: (KeyDef) -> Boolean, occurrence: Int = 0): Float {
        var x = 0f
        var seen = 0
        for (key in keys) {
            if (matches(key.def)) {
                if (seen == occurrence) return x
                seen++
            }
            x += key.width
        }
        error("no such key")
    }

    private fun char(primary: String): (KeyDef) -> Boolean = {
        it is KeyDef.Char && it.primary == primary
    }

    private fun KeyDef.isShiftedOf(key: KeyDef.Char): Boolean =
        this is KeyDef.Char && primary == key.shifted && key.shifted != key.primary

    private fun special(code: KeyCode): (KeyDef) -> Boolean = { it == KeyDef.Special(code) }

    private fun modifier(kind: ModifierKind): (KeyDef) -> Boolean = { it == KeyDef.Modifier(kind) }

    @Test
    fun `every classic row spans fourteen units`() {
        listOf(english, greek, estonian, finnish, fnPage).forEach { layout ->
            layout.rows.forEachIndexed { index, row ->
                assertEquals(
                    "${layout.id} row $index",
                    14.0,
                    row.keys.sumOf { it.width.toDouble() },
                    0.001,
                )
            }
        }
    }

    @Test
    fun `the english classic keys start where hacker's keyboard puts them`() {
        val rows = english.rows
        assertEquals(1f, rows[1].startOf(char("q")), 0.001f)
        assertEquals(1.5f, rows[2].startOf(char("a")), 0.001f)
        assertEquals(2f, rows[3].startOf(char("z")), 0.001f)
        assertEquals(13f, rows[3].startOf(modifier(ModifierKind.SHIFT), occurrence = 1), 0.001f)
        assertEquals(0f, rows[4].startOf(special(KeyCode.ESC)), 0.001f)
        assertEquals(
            1.5f,
            rows[4].startOf({ it == KeyDef.LayoutSwitch(BuiltInLayouts.SETTINGS) }),
            0.001f,
        )
        assertEquals(2.5f, rows[4].startOf(modifier(ModifierKind.ALT)), 0.001f)
        assertEquals(3.5f, rows[4].startOf(modifier(ModifierKind.META)), 0.001f)
        assertEquals(4.5f, rows[4].startOf(char(" ")), 0.001f)
        assertEquals(9f, rows[4].startOf(modifier(ModifierKind.FN)), 0.001f)
        assertEquals(10f, rows[4].startOf(special(KeyCode.DPAD_CENTER)), 0.001f)
        assertEquals(11f, rows[4].startOf(special(KeyCode.LEFT)), 0.001f)
    }

    @Test
    fun `greek classic keeps the english classic geometry`() {
        assertEquals(english.widths(), greek.widths())
    }

    @Test
    fun `estonian classic matches english except the shift row`() {
        listOf(0, 1, 2, 4).forEach {
            assertEquals("row $it", english.widths()[it], estonian.widths()[it])
        }
        val shiftRow = estonian.rows[3].keys
        assertEquals(KeyDef.Modifier(ModifierKind.SHIFT), shiftRow[0].def)
        assertEquals(1f, shiftRow[0].width, 0.001f)
        assertEquals("<", (shiftRow[1].def as KeyDef.Char).primary)
        assertEquals(1f, shiftRow[1].width, 0.001f)
    }

    @Test
    fun `the classic main layers have no fn variants`() {
        listOf(english, greek, estonian, finnish).forEach { layout ->
            assertTrue(layout.id, layout.rows.flatMap { it.keys }.none { it.fn != null })
        }
    }

    @Test
    fun `the fn page has the hacker's keyboard function and numpad keys`() {
        val defs = fnPage.rows.map { row -> row.keys.map { it.def } }
        listOf(KeyCode.F1, KeyCode.F2, KeyCode.F3, KeyCode.F4).forEach {
            assertTrue("$it", KeyDef.Special(it) in defs[0])
        }
        assertTrue(defs[0].any { it is KeyDef.Char && it.primary == "7" })
        assertTrue(KeyDef.Special(KeyCode.INSERT) in defs[1])
        assertTrue(KeyDef.Special(KeyCode.FORWARD_DEL) in defs[1])
        assertTrue(KeyDef.Special(KeyCode.SYSRQ) in defs[3])
        assertTrue(KeyDef.Special(KeyCode.NUM_LOCK) in defs[3])
    }

    @Test
    fun `every classic layout keeps its language's long press alternates`() {
        mapOf(
                english to BuiltInLayouts.PC_FULL_EN_US,
                greek to BuiltInLayouts.PC_FULL_EL,
                estonian to BuiltInLayouts.PC_FULL_ET,
                finnish to BuiltInLayouts.PC_FULL_FI,
            )
            .forEach { (classic, centredId) ->
                val centred =
                    load(centredId)
                        .rows
                        .flatMap { it.keys }
                        .mapNotNull { it.def as? KeyDef.Char }
                        .associateBy { it.primary }
                classic.rows
                    .flatMap { it.keys }
                    .mapNotNull { it.def as? KeyDef.Char }
                    .filter { it.primary in centred }
                    .forEach { key ->
                        val centredKey = centred.getValue(key.primary)
                        assertEquals(
                            "${classic.id} ${key.primary}",
                            centredKey.alternates.filterNot { it.isShiftedOf(centredKey) },
                            key.alternates.filterNot { it.isShiftedOf(key) },
                        )
                    }
            }
    }

    @Test
    fun `every full layout's symbol popups start with the shifted symbol`() {
        listOf(
                BuiltInLayouts.PC_FULL_EN_US,
                BuiltInLayouts.PC_FULL_EL,
                BuiltInLayouts.PC_FULL_ET,
                BuiltInLayouts.PC_FULL_FI,
            )
            .map(::load)
            .plus(listOf(english, greek, estonian, finnish))
            .forEach { layout ->
                layout.rows
                    .flatMap { it.keys }
                    .mapNotNull { it.def as? KeyDef.Char }
                    .filter { it.alternates.isNotEmpty() && it.shifted != it.primary }
                    .filter { it.primary.uppercase() == it.primary }
                    .forEach { key ->
                        assertEquals(
                            "${layout.id} ${key.primary}",
                            KeyDef.Char(key.shifted, key.shifted.uppercase()),
                            key.alternates.first(),
                        )
                    }
            }
    }

    @Test
    fun `finnish classic follows hacker's keyboard's finnish keys`() {
        assertEquals(estonian.widths(), finnish.widths())
        val defs = finnish.rows.map { row -> row.keys.map { it.def } }
        assertEquals(
            KeyDef.Char("§", "½"),
            (defs[0][0] as KeyDef.Char).copy(alternates = emptyList()),
        )
        assertTrue(defs[0].any { it is KeyDef.Char && it.primary == "?" && it.shifted == "+" })
        assertTrue(defs[0].any { it is KeyDef.Char && it.primary == "'" && it.shifted == "`" })
        assertTrue(defs[1].any { it is KeyDef.Char && it.primary == "å" })
        assertTrue(defs[1].any { it is KeyDef.Char && it.primary == "~" && it.shifted == "^" })
        assertTrue(finnish.rows.flatMap { it.keys }.none { it.def is KeyDef.Dead })
    }

    @Test
    fun `every classic bottom row has hacker's keyboard's esc and settings keys`() {
        listOf(english, greek, estonian, finnish, fnPage).forEach { layout ->
            val bottom = layout.rows.last().keys
            assertEquals(layout.id, KeyDef.Special(KeyCode.ESC), bottom[0].def)
            assertEquals(layout.id, 1.5f, bottom[0].width, 0.001f)
            assertEquals(layout.id, KeyDef.LayoutSwitch(BuiltInLayouts.SETTINGS), bottom[1].def)
        }
    }
}
