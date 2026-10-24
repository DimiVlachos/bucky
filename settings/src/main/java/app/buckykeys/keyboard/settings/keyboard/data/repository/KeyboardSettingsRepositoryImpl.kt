package app.buckykeys.keyboard.settings.keyboard.data.repository

import androidx.datastore.core.DataStore
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.repository.KeyboardSettingsRepository
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen

internal class KeyboardSettingsRepositoryImpl(
    private val dataStore: DataStore<KeyboardSettings>,
    private val defaults: KeyboardSettings,
) : KeyboardSettingsRepository {

    override val settings: Flow<KeyboardSettings> =
        dataStore.data.retryWhen { error, attempt ->
            if (error !is IOException) return@retryWhen false
            if (attempt == 0L) emit(defaults)
            delay(RETRY_DELAY_MS)
            true
        }

    override suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        dataStore.updateData(transform)
    }

    private companion object {
        const val RETRY_DELAY_MS = 1_000L
    }
}
