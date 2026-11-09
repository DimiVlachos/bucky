package app.buckykeys.keyboard.settings.keyboard.presentation.view_model.fake

import app.buckykeys.keyboard.settings.keyboard.domain.model.TouchOffset
import app.buckykeys.keyboard.settings.keyboard.domain.repository.TouchOffsetRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTouchOffsetRepository(initial: TouchOffset = TouchOffset.NONE) : TouchOffsetRepository {

    val stored = MutableStateFlow(initial)
    var failSaves = false

    override val offset: Flow<TouchOffset> = stored

    override suspend fun save(offset: TouchOffset) {
        if (failSaves) throw IOException("write failed")
        stored.value = offset
    }
}
