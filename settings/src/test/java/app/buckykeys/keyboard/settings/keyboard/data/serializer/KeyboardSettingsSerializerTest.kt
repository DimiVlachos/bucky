package app.buckykeys.keyboard.settings.keyboard.data.serializer

import androidx.datastore.core.CorruptionException
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.model.OutsideTerminals
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardSettingsSerializerTest {

    private val defaults = KeyboardSettings.defaults(systemLanguage = "en")
    private val serializer = KeyboardSettingsSerializer(defaults)

    private fun read(json: String): KeyboardSettings = runBlocking {
        serializer.readFrom(ByteArrayInputStream(json.encodeToByteArray()))
    }

    @Test
    fun `settings survive a write and a read`() {
        val settings =
            defaults
                .withLanguage(KeyboardLanguage.EL, enabled = true)
                .withRowHeight(52)
                .copy(keyPreview = false)
        val out = ByteArrayOutputStream()
        runBlocking { serializer.writeTo(settings, out) }
        assertEquals(settings, read(out.toByteArray().decodeToString()))
    }

    @Test
    fun `fields missing from an older file take their defaults`() {
        assertEquals(defaults.copy(rowHeightDp = 40), read("""{ "rowHeightDp": 40 }"""))
    }

    @Test
    fun `fields from a newer version are ignored`() {
        assertEquals(defaults, read("""{ "someFutureSetting": true }"""))
    }

    @Test
    fun `invalid json is reported as corruption so datastore falls back to defaults`() {
        val error = runCatching { read("""{ "rowHeightDp": """) }.exceptionOrNull()
        assertTrue("$error", error is CorruptionException)
    }

    @Test
    fun `a json value that is not an object is reported as corruption`() {
        val error = runCatching { read("""[1, 2]""") }.exceptionOrNull()
        assertTrue("$error", error is CorruptionException)
    }

    @Test
    fun `languages are stored by their constant names`() {
        val out = ByteArrayOutputStream()
        runBlocking {
            serializer.writeTo(defaults.withLanguage(KeyboardLanguage.EL, enabled = true), out)
        }
        assertTrue(out.toByteArray().decodeToString().contains(""""languages":["EN_US","EL"]"""))
    }

    @Test
    fun `an unknown language from a newer version is dropped and the rest is kept`() {
        assertEquals(
            defaults.copy(languages = listOf(KeyboardLanguage.EL), haptics = false),
            read("""{ "languages": ["DE", "EL"], "haptics": false }"""),
        )
    }

    @Test
    fun `a null field takes its default and the rest is kept`() {
        assertEquals(
            defaults.copy(rowHeightDp = 40),
            read("""{ "languages": null, "keyPreview": null, "rowHeightDp": 40 }"""),
        )
    }

    @Test
    fun `a field of the wrong type takes its default and the rest is kept`() {
        assertEquals(
            defaults.copy(keyPreview = false),
            read("""{ "rowHeightDp": "tall", "extraRow": 3, "keyPreview": false }"""),
        )
    }

    @Test
    fun `out of range heights are clamped`() {
        val settings = read("""{ "rowHeightDp": 0, "landscapeRowHeightDp": 900 }""")
        assertEquals(KeyboardSettings.ROW_HEIGHT_RANGE.first, settings.rowHeightDp)
        assertEquals(
            KeyboardSettings.LANDSCAPE_ROW_HEIGHT_RANGE.last,
            settings.landscapeRowHeightDp,
        )
    }

    @Test
    fun `settings saved before the label size existed load at one hundred percent`() {
        assertEquals(100, read("""{ "rowHeightDp": 48 }""").labelScalePercent)
    }

    @Test
    fun `an out of range label size is clamped`() {
        assertEquals(80, read("""{ "labelScalePercent": 5 }""").labelScalePercent)
    }

    @Test
    fun `settings saved before profiles load with compact`() {
        val settings = read("""{ "rowHeightDp": 48 }""")
        assertEquals(OutsideTerminals.COMPACT, settings.outsideTerminals)
        assertEquals(true, settings.autoCapitalize)
    }

    @Test
    fun `an unknown outside terminals value falls back to compact`() {
        assertEquals(
            OutsideTerminals.COMPACT,
            read("""{ "outsideTerminals": "SIDEWAYS" }""").outsideTerminals,
        )
    }

    @Test
    fun `a saved outside terminals value loads`() {
        assertEquals(
            OutsideTerminals.OTHER_KEYBOARD,
            read("""{ "outsideTerminals": "OTHER_KEYBOARD" }""").outsideTerminals,
        )
    }

    @Test
    fun `an empty language list falls back to the default languages`() {
        val greekDefaults = KeyboardSettings.defaults(systemLanguage = "el")
        val greekSerializer = KeyboardSettingsSerializer(greekDefaults)
        val settings = runBlocking {
            greekSerializer.readFrom(ByteArrayInputStream("""{ "languages": [] }""".toByteArray()))
        }
        assertEquals(greekDefaults.languages, settings.languages)
        assertEquals(defaults.languages, read("""{ "languages": ["DE"] }""").languages)
    }

    @Test
    fun `duplicate languages are read once`() {
        assertEquals(
            listOf(KeyboardLanguage.EL, KeyboardLanguage.EN_US),
            read("""{ "languages": ["EL", "EN_US", "EL"] }""").languages,
        )
    }

    @Test
    fun `the default value is the one it was given`() {
        assertEquals(defaults, serializer.defaultValue)
    }
}
