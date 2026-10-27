package app.buckykeys.keyboard.settings.keyboard.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class KeyboardLanguage(
    public val layoutId: String,
    public val compactLayoutId: String,
    public val classicLayoutId: String,
) {
    @SerialName("EN_US") EN_US("pc_full_en_us", "compact_en_us", "pc_hk_en_us"),
    @SerialName("EL") EL("pc_full_el", "compact_el", "pc_hk_el"),
    @SerialName("ET") ET("pc_full_et", "compact_et", "pc_hk_et"),
    @SerialName("FI") FI("pc_full_fi", "compact_fi", "pc_hk_fi"),
}
