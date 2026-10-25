package app.buckykeys.keyboard.ime.layout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LayoutTransformsTest {

    private val pcFull =
        LayoutParser.parse(File("src/main/assets/layouts/pc_full_en_us.json").readText())
            .getOrThrow()

    private val languageKey =
        KeySpec(KeyDef.LayoutSwitch(BuiltInLayouts.NEXT_LANGUAGE), "🌐", 1.25f)

    private val withLanguageKey =
        pcFull.copy(
            rows =
                pcFull.rows.dropLast(1) +
                    Row(
                        listOf(pcFull.rows.last().keys.first(), languageKey) +
                            pcFull.rows.last().keys.drop(1).let { keys ->
                                keys.map {
                                    if (it.def == KeyDef.Char(" ", " "))
                                        it.copy(width = it.width - 1.25f)
                                    else it
                                }
                            },
                    ),
        )

    @Test
    fun `removing the language key gives its width to the space bar`() {
        val bottom = withLanguageKey.withoutLanguageKey().rows.last().keys
        assertFalse(bottom.any { it.def == KeyDef.LayoutSwitch(BuiltInLayouts.NEXT_LANGUAGE) })
        val space = pcFull.rows.last().keys.first { it.def == KeyDef.Char(" ", " ") }
        assertEquals(space.width, bottom.first { it.def == space.def }.width, 0.001f)
    }

    @Test
    fun `every row still spans fifteen units`() {
        withLanguageKey.withoutLanguageKey().rows.forEach { row ->
            assertEquals(15f, row.keys.sumOf { it.width.toDouble() }.toFloat(), 0.001f)
        }
    }

    @Test
    fun `rows without a language key are untouched`() {
        assertEquals(pcFull.rows.dropLast(1), withLanguageKey.withoutLanguageKey().rows.dropLast(1))
    }

    @Test
    fun `the built-in layout has no language key and is returned as it is`() {
        assertEquals(pcFull, pcFull.withoutLanguageKey())
    }

    @Test
    fun `a row holding only the language key is dropped`() {
        val layout =
            Layout(
                "t",
                "T",
                listOf(
                    Row(listOf(KeySpec(KeyDef.Special(KeyCode.ESC), "Esc"))),
                    Row(listOf(KeySpec(KeyDef.LayoutSwitch(BuiltInLayouts.NEXT_LANGUAGE), "🌐"))),
                ),
            )
        assertEquals(layout.rows.take(1), layout.withoutLanguageKey().rows)
    }

    @Test
    fun `removing the keyboard key gives its width to the widest key`() {
        val layout =
            Layout(
                "t",
                "T",
                listOf(
                    Row(
                        listOf(
                            KeySpec(KeyDef.Char(" ", " "), "", 4f),
                            KeySpec(KeyDef.LayoutSwitch(BuiltInLayouts.OTHER_KEYBOARD), "⌨"),
                        ),
                    ),
                ),
            )
        val row = layout.withoutSwitchKey(BuiltInLayouts.OTHER_KEYBOARD).rows.single()
        assertEquals(listOf(5f), row.keys.map { it.width })
    }

    @Test
    fun `a keyboard key can become a tap to full key`() {
        val layout =
            Layout(
                "t",
                "T",
                listOf(
                    Row(
                        listOf(
                            KeySpec(
                                KeyDef.LayoutSwitch(
                                    BuiltInLayouts.OTHER_KEYBOARD,
                                    BuiltInLayouts.FULL_LAYOUT,
                                ),
                                "⌨",
                            ),
                        ),
                    ),
                ),
            )
        val key =
            layout
                .withSwitchTarget(BuiltInLayouts.OTHER_KEYBOARD, BuiltInLayouts.FULL_LAYOUT)
                .rows
                .single()
                .keys
                .single()
        assertEquals(KeyDef.LayoutSwitch(BuiltInLayouts.FULL_LAYOUT), key.def)
        assertEquals("⌨", key.label)
    }
}
