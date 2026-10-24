package app.buckykeys.keyboard.settings.keyboard.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class KeyboardLanguage(
    public val layoutId: String,
    public val compactLayoutId: String,
) {
    @SerialName("EN_US") EN_US("pc_full_en_us", "compact_en_us"),
    @SerialName("EL") EL("pc_full_el", "compact_el"),
}
