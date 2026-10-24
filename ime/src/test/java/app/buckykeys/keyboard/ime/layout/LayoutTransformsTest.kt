package app.buckykeys.keyboard.ime.layout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LayoutTransformsTest {

    private val pcFull =
        LayoutParser.parse(File("src/main/assets/layouts/pc_full_en_us.json").readText())
            .getOrThrow()

    @Test
    fun `removing the language key gives its width to the space bar`() {
        val bottom = pcFull.withoutLanguageKey().rows.last().keys
        assertFalse(bottom.any { it.def == KeyDef.LayoutSwitch(BuiltInLayouts.NEXT_LANGUAGE) })
        assertEquals(6.75f, bottom.first { it.def == KeyDef.Char(" ", " ") }.width, 0.001f)
    }

    @Test
    fun `every row still spans fifteen units`() {
        pcFull.withoutLanguageKey().rows.forEach { row ->
            assertEquals(15f, row.keys.sumOf { it.width.toDouble() }.toFloat(), 0.001f)
        }
    }

    @Test
    fun `rows without a language key are untouched`() {
        assertEquals(pcFull.rows.dropLast(1), pcFull.withoutLanguageKey().rows.dropLast(1))
    }

    @Test
    fun `a layout without a language key is returned as it is`() {
        val once = pcFull.withoutLanguageKey()
        assertEquals(once, once.withoutLanguageKey())
    }
}
