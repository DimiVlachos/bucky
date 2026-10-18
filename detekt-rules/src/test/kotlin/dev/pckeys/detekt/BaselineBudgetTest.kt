package dev.pckeys.detekt

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaselineBudgetTest {

    private val budgets =
        mapOf(
            "CatchBlockHygiene" to 0,
            "HardcodedColor" to 0,
            "TimberErrorArgument" to 0,
            "NoNotNullAssertion" to 0,
        )

    @Test
    fun `baselined pckeys findings can only shrink`() {
        val baseline = File(requireNotNull(System.getProperty("pckeys.detekt.baseline")))
        assertTrue("baseline not found at ${baseline.absolutePath}", baseline.isFile)
        val counts =
            Regex("<ID>([A-Za-z]+):")
                .findAll(baseline.readText())
                .groupingBy { it.groupValues[1] }
                .eachCount()
        budgets.forEach { (rule, budget) ->
            val actual = counts[rule] ?: 0
            assertEquals(
                when {
                    actual > budget ->
                        "$rule: baseline grew to $actual (budget $budget) — fix the new violation, do not baseline it"
                    actual < budget ->
                        "$rule: baseline shrank to $actual — lower the budget in BaselineBudgetTest to $actual"
                    else -> ""
                },
                budget,
                actual,
            )
        }
    }
}
