package app.buckykeys.keyboard.billing.foss.data.repository

import app.buckykeys.keyboard.billing.domain.model.ProEntitlement
import app.buckykeys.keyboard.billing.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class FossBillingRepository : BillingRepository {
    override val entitlement: StateFlow<ProEntitlement> =
        MutableStateFlow(ProEntitlement.Unlocked).asStateFlow()
}
