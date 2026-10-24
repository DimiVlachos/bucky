package app.buckykeys.keyboard.settings.keyboard.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder

internal object KeyboardLanguageListSerializer : KSerializer<List<KeyboardLanguage>> {

    private val delegate = ListSerializer(KeyboardLanguage.serializer())

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: List<KeyboardLanguage>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<KeyboardLanguage> {
        val jsonDecoder = decoder as? JsonDecoder ?: return delegate.deserialize(decoder)
        val array = jsonDecoder.decodeJsonElement() as? JsonArray ?: return emptyList()
        return array.mapNotNull { element ->
            try {
                jsonDecoder.json.decodeFromJsonElement(KeyboardLanguage.serializer(), element)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
