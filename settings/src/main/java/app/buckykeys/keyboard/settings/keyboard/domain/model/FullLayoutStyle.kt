package app.buckykeys.keyboard.settings.keyboard.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class FullLayoutStyle {
    @SerialName("CENTRED") CENTRED,
    @SerialName("HK_CLASSIC") HK_CLASSIC,
}
