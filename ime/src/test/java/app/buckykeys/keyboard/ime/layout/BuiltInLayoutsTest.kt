package app.buckykeys.keyboard.ime.layout

import java.io.File
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class BuiltInLayoutsTest {

    private fun asset(id: String): String = File("src/main/assets/layouts/$id.json").readText()

    @Test
    fun `the pc full asset matches the fallback layout`() {
        assertEquals(
            FallbackLayout.pcFullEnUs,
            LayoutParser.parse(asset(BuiltInLayouts.PC_FULL_EN_US)).getOrThrow(),
        )
    }

    @Test
    fun `a layout that parses is used as it is`() {
        val json = asset(BuiltInLayouts.PC_FULL_EN_US).replace("PC Full (US)", "From the asset")

        assertEquals("From the asset", BuiltInLayouts.loadOrFallback { json }.name)
    }

    @Test
    fun `a layout that fails to parse falls back to the built-in layout`() {
        assertEquals(FallbackLayout.pcFullEnUs, BuiltInLayouts.loadOrFallback { """{ "id": """ })
    }

    @Test
    fun `a missing asset falls back to the built-in layout`() {
        assertEquals(
            FallbackLayout.pcFullEnUs,
            BuiltInLayouts.loadOrFallback { throw IOException("layouts/pc_full_en_us.json") },
        )
    }

    @Test
    fun `the greek layout parses`() {
        assertEquals(
            "pc_full_el",
            LayoutParser.parse(asset(BuiltInLayouts.PC_FULL_EL)).getOrThrow().id,
        )
    }

    @Test
    fun `the greek layout keeps every key of the english one in place`() {
        val en = LayoutParser.parse(asset(BuiltInLayouts.PC_FULL_EN_US)).getOrThrow()
        val el = LayoutParser.parse(asset(BuiltInLayouts.PC_FULL_EL)).getOrThrow()
        assertEquals(
            en.rows.map { row -> row.keys.map { it.width } },
            el.rows.map { row -> row.keys.map { it.width } },
        )
        en.rows.zip(el.rows).forEach { (enRow, elRow) ->
            enRow.keys.zip(elRow.keys).forEach { (enKey, elKey) ->
                val enDef = enKey.def
                val elDef = elKey.def
                val latin =
                    when (elDef) {
                        is KeyDef.Char -> elDef.latin ?: elDef.primary
                        is KeyDef.Dead -> elDef.latin
                        else -> null
                    }
                if (enDef is KeyDef.Char) assertEquals(elKey.label, enDef.primary, latin)
                else assertEquals(enDef, elDef)
            }
        }
    }

    @Test
    fun `every greek vowel composes with the tonos`() {
        val el = LayoutParser.parse(asset(BuiltInLayouts.PC_FULL_EL)).getOrThrow()
        val chars =
            el.rows
                .flatMap { it.keys }
                .mapNotNull { it.def as? KeyDef.Char }
                .associateBy { it.primary }
        mapOf("α" to "ά", "ε" to "έ", "η" to "ή", "ι" to "ί", "ο" to "ό", "υ" to "ύ", "ω" to "ώ")
            .forEach { (vowel, accented) ->
                assertEquals(vowel, accented, chars.getValue(vowel).composed["΄"])
            }
        assertEquals("ϊ", chars.getValue("ι").composed["¨"])
        assertEquals("Ϋ", chars.getValue("υ").composedShifted["¨"])
    }

    @Test
    fun `every greek capital vowel composes with the tonos and the dialytika`() {
        val chars = greekChars()
        mapOf("α" to "Ά", "ε" to "Έ", "η" to "Ή", "ι" to "Ί", "ο" to "Ό", "υ" to "Ύ", "ω" to "Ώ")
            .forEach { (vowel, accented) ->
                assertEquals(vowel, accented, chars.getValue(vowel).composedShifted["΄"])
            }
        assertEquals("ϋ", chars.getValue("υ").composed["¨"])
        assertEquals("Ϊ", chars.getValue("ι").composedShifted["¨"])
        assertEquals("Ϋ", chars.getValue("υ").composedShifted["¨"])
    }

    @Test
    fun `greek epsilon offers the euro sign on long press`() {
        assertEquals(
            listOf(KeyDef.Char("έ", "Έ"), KeyDef.Char("€", "€")),
            greekChars().getValue("ε").alternates,
        )
    }

    @Test
    fun `every estonian row spans fifteen units`() {
        estonian().rows.forEachIndexed { index, row ->
            assertEquals("row $index", 15.0, row.keys.sumOf { it.width.toDouble() }, 0.001)
        }
    }

    @Test
    fun `the estonian letters send their us key with ctrl`() {
        val chars = estonianChars()
        mapOf("ü" to "[", "õ" to "]", "ö" to ";", "ä" to "'").forEach { (letter, latin) ->
            assertEquals(letter, latin, chars.getValue(letter).latin)
        }
    }

    @Test
    fun `every estonian altgr symbol is the first long press of its key`() {
        val firstAlternates =
            estonianChars().values.mapNotNull {
                (it.alternates.firstOrNull() as? KeyDef.Char)?.primary
            }
        listOf("@", "£", "$", "€", "{", "[", "]", "}", "\\", "|", "š", "ž").forEach {
            assertEquals(it, true, it in firstAlternates)
        }
    }

    @Test
    fun `the estonian caron and tilde dead keys compose the estonian letters`() {
        val chars = estonianChars()
        assertEquals("š", chars.getValue("s").composed["ˇ"])
        assertEquals("Ž", chars.getValue("z").composedShifted["ˇ"])
        assertEquals("õ", chars.getValue("o").composed["~"])
    }

    @Test
    fun `the estonian number row keeps every f key on fn`() {
        val numberRow = estonian().rows.first().keys
        assertEquals(
            (1..12).map { KeyDef.Special(KeyCode.valueOf("F$it")) },
            numberRow.mapNotNull { it.fn },
        )
    }

    private fun estonian(): Layout =
        LayoutParser.parse(asset(BuiltInLayouts.PC_FULL_ET)).getOrThrow()

    private fun estonianChars(): Map<String, KeyDef.Char> =
        estonian()
            .rows
            .flatMap { it.keys }
            .mapNotNull { it.def as? KeyDef.Char }
            .associateBy {
                it.primary
            }

    private fun greekChars(): Map<String, KeyDef.Char> =
        LayoutParser.parse(asset(BuiltInLayouts.PC_FULL_EL))
            .getOrThrow()
            .rows
            .flatMap { it.keys }
            .mapNotNull { it.def as? KeyDef.Char }
            .associateBy { it.primary }
}
