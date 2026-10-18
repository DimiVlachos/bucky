package dev.pckeys.keyboard.billing.foss.data.repository

import dev.pckeys.keyboard.billing.domain.model.ProEntitlement
import org.junit.Assert.assertEquals
import org.junit.Test

class FossBillingRepositoryTest {

    @Test
    fun `entitlement is always unlocked`() {
        assertEquals(ProEntitlement.Unlocked, FossBillingRepository().entitlement.value)
    }
}
