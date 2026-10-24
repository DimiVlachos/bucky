package app.buckykeys.keyboard.settings.keyboard.domain.model

import kotlinx.serialization.Serializable

@Serializable
public enum class KeyboardLanguage(public val layoutId: String) {
    EN_US("pc_full_en_us"),
    EL("pc_full_el"),
}
