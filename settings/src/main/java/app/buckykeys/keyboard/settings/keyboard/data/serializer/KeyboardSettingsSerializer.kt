package app.buckykeys.keyboard.settings.keyboard.data.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

internal class KeyboardSettingsSerializer(override val defaultValue: KeyboardSettings) :
    Serializer<KeyboardSettings> {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    override suspend fun readFrom(input: InputStream): KeyboardSettings {
        val root =
            try {
                json.parseToJsonElement(input.readBytes().decodeToString())
            } catch (e: SerializationException) {
                throw CorruptionException("keyboard settings are not valid JSON", e)
            }
        if (root !is JsonObject) throw CorruptionException("keyboard settings are not an object")
        val readable = JsonObject(root.filter { (key, value) -> decodes(key, value) })
        val settings =
            decode(readable) ?: throw CorruptionException("keyboard settings are invalid")
        return settings.sanitized(defaultValue.languages)
    }

    override suspend fun writeTo(t: KeyboardSettings, output: OutputStream) {
        output.write(json.encodeToString(KeyboardSettings.serializer(), t).encodeToByteArray())
    }

    private fun decodes(key: String, value: JsonElement): Boolean =
        decode(JsonObject(mapOf(key to value))) != null

    private fun decode(element: JsonObject): KeyboardSettings? =
        try {
            json.decodeFromJsonElement(KeyboardSettings.serializer(), element)
        } catch (e: IllegalArgumentException) {
            null
        }
}
