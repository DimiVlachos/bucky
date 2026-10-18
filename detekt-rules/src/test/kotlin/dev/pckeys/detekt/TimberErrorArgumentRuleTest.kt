package dev.pckeys.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class TimberErrorArgumentRuleTest {

    private val rule = TimberErrorArgumentRule(Config.empty)

    @Test
    fun `single-arg Timber e is reported`() {
        assertEquals(1, rule.lint("fun f() { Timber.e(\"failed\") }").size)
    }

    @Test
    fun `two-arg Timber e with throwable is not reported`() {
        assertEquals(0, rule.lint("fun f(t: Throwable) { Timber.e(t, \"failed\") }").size)
    }

    @Test
    fun `Timber e with null throwable is not reported`() {
        assertEquals(0, rule.lint("fun f() { Timber.e(null, \"failed\") }").size)
    }
}
