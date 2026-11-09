package app.buckykeys.keyboard.settings.keyboard.data.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import app.buckykeys.keyboard.settings.keyboard.domain.model.TouchOffset
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal class TouchOffsetSerializer : Serializer<TouchOffset> {

    override val defaultValue: TouchOffset = TouchOffset.NONE

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun readFrom(input: InputStream): TouchOffset =
        try {
            json.decodeFromString(TouchOffset.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("touch offset is not valid JSON", e)
        } catch (e: IllegalArgumentException) {
            throw CorruptionException("touch offset is invalid", e)
        }

    override suspend fun writeTo(t: TouchOffset, output: OutputStream) {
        output.write(json.encodeToString(TouchOffset.serializer(), t).encodeToByteArray())
    }
}
