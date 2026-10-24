package app.buckykeys.keyboard.settings.keyboard.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardSettingsTest {

    private val english = KeyboardSettings(languages = listOf(KeyboardLanguage.EN_US))

    @Test
    fun `enabling a language appends it`() {
        assertEquals(
            listOf(KeyboardLanguage.EN_US, KeyboardLanguage.EL),
            english.withLanguage(KeyboardLanguage.EL, enabled = true).languages,
        )
    }

    @Test
    fun `enabling a language twice keeps one copy`() {
        val both = english.withLanguage(KeyboardLanguage.EL, enabled = true)
        assertEquals(both, both.withLanguage(KeyboardLanguage.EL, enabled = true))
    }

    @Test
    fun `disabling a language removes it`() {
        val both = english.withLanguage(KeyboardLanguage.EL, enabled = true)
        assertEquals(
            listOf(KeyboardLanguage.EL),
            both.withLanguage(KeyboardLanguage.EN_US, enabled = false).languages,
        )
    }

    @Test
    fun `the last language cannot be disabled`() {
        assertEquals(english, english.withLanguage(KeyboardLanguage.EN_US, enabled = false))
    }

    @Test
    fun `row heights are clamped to their ranges`() {
        assertEquals(KeyboardSettings.ROW_HEIGHT_RANGE.last, english.withRowHeight(500).rowHeightDp)
        assertEquals(KeyboardSettings.ROW_HEIGHT_RANGE.first, english.withRowHeight(0).rowHeightDp)
        assertEquals(
            KeyboardSettings.LANDSCAPE_ROW_HEIGHT_RANGE.first,
            english.withLandscapeRowHeight(0).landscapeRowHeightDp,
        )
    }

    @Test
    fun `a greek system adds greek to the default languages`() {
        assertEquals(
            listOf(KeyboardLanguage.EN_US, KeyboardLanguage.EL),
            KeyboardSettings.defaults(systemLanguage = "el").languages,
        )
        assertEquals(
            listOf(KeyboardLanguage.EN_US),
            KeyboardSettings.defaults(systemLanguage = "de").languages,
        )
    }

    @Test
    fun `the defaults keep the portrait and landscape heights the spike used`() {
        val defaults = KeyboardSettings.defaults(systemLanguage = "en")
        assertEquals(48, defaults.rowHeightDp)
        assertEquals(34, defaults.landscapeRowHeightDp)
    }

    @Test
    fun `the extra row is split into words`() {
        assertEquals(
            listOf("ESC", "TAB", "~", "|"),
            english.withExtraRow("  ESC TAB\t~   |\n").extraRow,
        )
    }

    @Test
    fun `the extra row keeps at most fifteen words`() {
        assertEquals(15, english.withExtraRow(List(20) { "/" }.joinToString(" ")).extraRow.size)
    }

    @Test
    fun `an empty extra row clears it`() {
        assertEquals(emptyList<String>(), english.withExtraRow("ESC").withExtraRow("   ").extraRow)
    }

    @Test
    fun `no extra rows by default`() {
        val defaults = KeyboardSettings.defaults(systemLanguage = "en")
        assertEquals(false, defaults.fKeyRow)
        assertEquals(emptyList<String>(), defaults.extraRow)
    }
}
