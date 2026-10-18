package dev.pckeys.keyboard.billing.foss.data.repository

import dev.pckeys.keyboard.billing.domain.model.ProEntitlement
import dev.pckeys.keyboard.billing.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class FossBillingRepository : BillingRepository {
    override val entitlement: StateFlow<ProEntitlement> =
        MutableStateFlow(ProEntitlement.Unlocked).asStateFlow()
}
