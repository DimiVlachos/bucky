package app.buckykeys.keyboard.settings.keyboard.presentation.state

import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings

internal data class KeyboardSettingsState(
    val settings: KeyboardSettings? = null,
    val saveFailed: Boolean = false,
)
