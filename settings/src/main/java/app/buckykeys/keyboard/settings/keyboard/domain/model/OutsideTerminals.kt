package app.buckykeys.keyboard.settings.keyboard.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class OutsideTerminals {
    @SerialName("COMPACT") COMPACT,
    @SerialName("FULL") FULL,
    @SerialName("OTHER_KEYBOARD") OTHER_KEYBOARD,
}
