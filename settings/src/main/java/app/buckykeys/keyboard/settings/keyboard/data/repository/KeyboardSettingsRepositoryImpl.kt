package app.buckykeys.keyboard.settings.keyboard.data.repository

import androidx.datastore.core.DataStore
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.repository.KeyboardSettingsRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

internal class KeyboardSettingsRepositoryImpl(
    private val dataStore: DataStore<KeyboardSettings>,
    private val defaults: KeyboardSettings,
) : KeyboardSettingsRepository {

    override val settings: Flow<KeyboardSettings> =
        dataStore.data.catch { error ->
            if (error is IOException) emit(defaults) else throw error
        }

    override suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        dataStore.updateData(transform)
    }
}
