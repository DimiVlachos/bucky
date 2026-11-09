package app.buckykeys.keyboard.settings.keyboard.domain.repository

import app.buckykeys.keyboard.settings.keyboard.domain.model.TouchOffset
import kotlinx.coroutines.flow.Flow

public interface TouchOffsetRepository {
    public val offset: Flow<TouchOffset>

    public suspend fun save(offset: TouchOffset)
}
