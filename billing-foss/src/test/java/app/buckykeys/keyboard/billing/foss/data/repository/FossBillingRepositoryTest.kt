package app.buckykeys.keyboard.billing.foss.data.repository

import app.buckykeys.keyboard.billing.domain.model.ProEntitlement
import org.junit.Assert.assertEquals
import org.junit.Test

class FossBillingRepositoryTest {

    @Test
    fun `entitlement is always unlocked`() {
        assertEquals(ProEntitlement.Unlocked, FossBillingRepository().entitlement.value)
    }
}
