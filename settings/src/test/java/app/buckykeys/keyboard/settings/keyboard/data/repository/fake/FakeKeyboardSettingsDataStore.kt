package app.buckykeys.keyboard.settings.keyboard.data.repository.fake

import androidx.datastore.core.DataStore
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class FakeKeyboardSettingsDataStore(initial: KeyboardSettings) : DataStore<KeyboardSettings> {

    val stored = MutableStateFlow(initial)
    var readFailuresLeft = 0

    override val data: Flow<KeyboardSettings> = flow {
        if (readFailuresLeft > 0) {
            readFailuresLeft--
            throw IOException("read failed")
        }
        emitAll(stored)
    }

    override suspend fun updateData(
        transform: suspend (t: KeyboardSettings) -> KeyboardSettings,
    ): KeyboardSettings = transform(stored.value).also { stored.value = it }
}
