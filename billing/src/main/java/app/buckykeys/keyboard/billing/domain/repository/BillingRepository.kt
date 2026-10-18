package app.buckykeys.keyboard.billing.domain.repository

import app.buckykeys.keyboard.billing.domain.model.ProEntitlement
import kotlinx.coroutines.flow.StateFlow

public interface BillingRepository {
    public val entitlement: StateFlow<ProEntitlement>
}
