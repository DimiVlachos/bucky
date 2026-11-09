package app.buckykeys.keyboard.settings.keyboard.data.repository

import androidx.datastore.core.DataStore
import app.buckykeys.keyboard.settings.keyboard.domain.model.TouchOffset
import app.buckykeys.keyboard.settings.keyboard.domain.repository.TouchOffsetRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

internal class TouchOffsetRepositoryImpl(private val dataStore: DataStore<TouchOffset>) :
    TouchOffsetRepository {

    override val offset: Flow<TouchOffset> =
        dataStore.data.catch { error ->
            if (error !is IOException) throw error
            emit(TouchOffset.NONE)
        }

    override suspend fun save(offset: TouchOffset) {
        dataStore.updateData { offset }
    }
}
