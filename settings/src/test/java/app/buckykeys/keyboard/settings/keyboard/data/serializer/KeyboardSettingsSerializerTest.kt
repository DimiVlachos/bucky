package app.buckykeys.keyboard.settings.keyboard.data.serializer

import androidx.datastore.core.CorruptionException
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
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
    fun `the default value is the one it was given`() {
        assertEquals(defaults, serializer.defaultValue)
    }
}
