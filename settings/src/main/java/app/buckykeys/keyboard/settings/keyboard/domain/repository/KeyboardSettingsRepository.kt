package app.buckykeys.keyboard.settings.keyboard.domain.repository

import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import kotlinx.coroutines.flow.Flow

public interface KeyboardSettingsRepository {
    public val settings: Flow<KeyboardSettings>

    public suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings)
}
