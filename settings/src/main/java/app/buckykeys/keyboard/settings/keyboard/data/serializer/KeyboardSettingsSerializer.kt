package app.buckykeys.keyboard.settings.keyboard.data.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal class KeyboardSettingsSerializer(override val defaultValue: KeyboardSettings) :
    Serializer<KeyboardSettings> {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun readFrom(input: InputStream): KeyboardSettings =
        try {
            json.decodeFromString(KeyboardSettings.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("keyboard settings are not valid JSON", e)
        }

    override suspend fun writeTo(t: KeyboardSettings, output: OutputStream) {
        output.write(json.encodeToString(KeyboardSettings.serializer(), t).encodeToByteArray())
    }
}
