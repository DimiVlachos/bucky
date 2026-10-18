package app.buckykeys.keyboard.billing.play.data.repository

import app.buckykeys.keyboard.billing.domain.model.ProEntitlement
import app.buckykeys.keyboard.billing.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class PlayBillingRepository : BillingRepository {
    override val entitlement: StateFlow<ProEntitlement> =
        MutableStateFlow(ProEntitlement.Unknown).asStateFlow()
}
