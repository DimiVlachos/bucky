package app.buckykeys.detekt

import io.gitlab.arturbosch.detekt.api.Config
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleRegistrationTest {

    private val ruleIds = BuckyRuleSetProvider().instance(Config.empty).rules.map { it.ruleId }

    @Test
    fun `every registered rule is active in detekt yml`() {
        val lines = File(requireNotNull(System.getProperty("bucky.detekt.config"))).readLines()
        val buckyBlock =
            lines
                .dropWhile { it != "bucky:" }
                .drop(1)
                .takeWhile { it.isBlank() || it.first().isWhitespace() }
        val activeIds =
            buckyBlock
                .zipWithNext()
                .filter { (rule, next) ->
                    Regex("""^  \w+:$""").matches(rule) && next.trim() == "active: true"
                }
                .map { (rule, _) -> rule.trim().removeSuffix(":") }
                .toSet()
        assertEquals(ruleIds.toSet(), activeIds)
    }

    @Test
    fun `every registered rule has a test class`() {
        val testDir = File(requireNotNull(System.getProperty("bucky.detekt.testDir")))
        val testFiles = testDir.walkTopDown().filter { it.isFile }.map { it.name }.toSet()
        val untested = ruleIds.filterNot { "${it}RuleTest.kt" in testFiles }
        assertTrue("rules without a *RuleTest: $untested", untested.isEmpty())
    }
}
