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
    fun `a finnish system adds finnish to the default languages`() {
        assertEquals(
            listOf(KeyboardLanguage.EN_US, KeyboardLanguage.FI),
            KeyboardSettings.defaults(systemLanguage = "fi").languages,
        )
    }

    @Test
    fun `an estonian system adds estonian to the default languages`() {
        assertEquals(
            listOf(KeyboardLanguage.EN_US, KeyboardLanguage.ET),
            KeyboardSettings.defaults(systemLanguage = "et").languages,
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
    fun `the label size defaults to one hundred percent`() {
        assertEquals(100, KeyboardSettings.defaults(systemLanguage = "en").labelScalePercent)
    }

    @Test
    fun `the label size snaps to ten percent steps within its range`() {
        assertEquals(110, english.withLabelScale(113).labelScalePercent)
        assertEquals(200, english.withLabelScale(500).labelScalePercent)
        assertEquals(170, english.withLabelScale(168).labelScalePercent)
        assertEquals(80, english.withLabelScale(0).labelScalePercent)
    }

    @Test
    fun `sanitized clamps the label size`() {
        assertEquals(200, english.copy(labelScalePercent = 999).sanitized().labelScalePercent)
    }

    @Test
    fun `apps default to compact with both helpers on`() {
        val defaults = KeyboardSettings.defaults(systemLanguage = "en")
        assertEquals(OutsideTerminals.COMPACT, defaults.outsideTerminals)
        assertEquals(true, defaults.autoCapitalize)
        assertEquals(true, defaults.doubleSpacePeriod)
    }

    @Test
    fun `clearing the field counts as a change to a saved row`() {
        assertEquals(true, english.copy(extraRow = listOf("ESC")).extraRowChangedBy(" "))
    }

    @Test
    fun `the same words with other spacing are not a change`() {
        assertEquals(
            false,
            english.copy(extraRow = listOf("ESC", "TAB")).extraRowChangedBy(" ESC   TAB "),
        )
    }

    @Test
    fun `different words are a change`() {
        assertEquals(true, english.copy(extraRow = listOf("ESC")).extraRowChangedBy("ESC TAB"))
    }

    @Test
    fun `a huge label size clamps to the maximum`() {
        assertEquals(200, english.withLabelScale(Int.MAX_VALUE).labelScalePercent)
    }

    @Test
    fun `the extra row is split into words`() {
        assertEquals(
            listOf("ESC", "TAB", "~", "|"),
            english.withExtraRow("  ESC TAB\t~   |\n").extraRow,
        )
    }

    @Test
    fun `the extra row keeps more than fifteen words so invalid ones do not use up key slots`() {
        assertEquals(20, english.withExtraRow(List(20) { "/" }.joinToString(" ")).extraRow.size)
    }

    @Test
    fun `the extra row is capped to bound storage`() {
        assertEquals(
            KeyboardSettings.MAX_EXTRA_ROW_WORDS,
            english.withExtraRow(List(100) { "/" }.joinToString(" ")).extraRow.size,
        )
    }

    @Test
    fun `sanitized removes duplicate languages`() {
        val settings = english.copy(languages = listOf(KeyboardLanguage.EL, KeyboardLanguage.EL))
        assertEquals(listOf(KeyboardLanguage.EL), settings.sanitized().languages)
    }

    @Test
    fun `sanitized replaces an empty language list with the fallback`() {
        val settings = english.copy(languages = emptyList())
        assertEquals(listOf(KeyboardLanguage.EN_US), settings.sanitized().languages)
        assertEquals(
            listOf(KeyboardLanguage.EL),
            settings.sanitized(fallbackLanguages = listOf(KeyboardLanguage.EL)).languages,
        )
        assertEquals(
            listOf(KeyboardLanguage.EN_US),
            settings.sanitized(fallbackLanguages = emptyList()).languages,
        )
    }

    @Test
    fun `sanitized clamps both heights`() {
        val settings = english.copy(rowHeightDp = 0, landscapeRowHeightDp = 1_000).sanitized()
        assertEquals(KeyboardSettings.ROW_HEIGHT_RANGE.first, settings.rowHeightDp)
        assertEquals(
            KeyboardSettings.LANDSCAPE_ROW_HEIGHT_RANGE.last,
            settings.landscapeRowHeightDp,
        )
    }

    @Test
    fun `sanitized drops blank extra keys and caps the extra row`() {
        val settings = english.copy(extraRow = listOf("ESC", " ", "") + List(100) { "/" })
        val extraRow = settings.sanitized().extraRow
        assertEquals("ESC", extraRow.first())
        assertEquals(KeyboardSettings.MAX_EXTRA_ROW_WORDS, extraRow.size)
    }

    @Test
    fun `sanitized keeps valid settings unchanged`() {
        val settings = KeyboardSettings.defaults(systemLanguage = "el").withExtraRow("ESC TAB")
        assertEquals(settings, settings.sanitized())
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
