package app.buckykeys.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class NoNotNullAssertionRuleTest {

    private val rule = NoNotNullAssertionRule(Config.empty)

    @Test
    fun `not-null assertion is reported`() {
        assertEquals(1, rule.lint("fun f(s: String?) = s!!.length").size)
    }

    @Test
    fun `safe call is not reported`() {
        assertEquals(0, rule.lint("fun f(s: String?) = s?.length ?: 0").size)
    }
}
