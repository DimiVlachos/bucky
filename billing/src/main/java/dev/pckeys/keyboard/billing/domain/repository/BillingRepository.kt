package dev.pckeys.keyboard.billing.domain.repository

import dev.pckeys.keyboard.billing.domain.model.ProEntitlement
import kotlinx.coroutines.flow.StateFlow

public interface BillingRepository {
    public val entitlement: StateFlow<ProEntitlement>
}
