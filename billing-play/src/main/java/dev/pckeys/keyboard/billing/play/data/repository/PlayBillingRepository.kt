package dev.pckeys.keyboard.billing.play.data.repository

import dev.pckeys.keyboard.billing.domain.model.ProEntitlement
import dev.pckeys.keyboard.billing.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class PlayBillingRepository : BillingRepository {
    override val entitlement: StateFlow<ProEntitlement> =
        MutableStateFlow(ProEntitlement.Unknown).asStateFlow()
}
