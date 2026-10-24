package app.buckykeys.keyboard.settings.keyboard.presentation.view_model.fake

import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.repository.KeyboardSettingsRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeKeyboardSettingsRepository(initial: KeyboardSettings) : KeyboardSettingsRepository {

    val stored = MutableStateFlow(initial)
    var failUpdates = false

    override val settings: Flow<KeyboardSettings> = stored

    override suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        if (failUpdates) throw IOException("write failed")
        stored.value = transform(stored.value)
    }
}
