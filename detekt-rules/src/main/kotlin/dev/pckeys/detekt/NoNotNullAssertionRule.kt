package dev.pckeys.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtPostfixExpression

class NoNotNullAssertionRule(config: Config) : Rule(config) {
    override val issue =
        Issue(
            id = "NoNotNullAssertion",
            severity = Severity.Defect,
            description =
                "Avoid the !! operator; handle nullability explicitly (safe calls, requireNotNull, " +
                    "early return).",
            debt = Debt.FIVE_MINS,
        )

    override fun visitPostfixExpression(expression: KtPostfixExpression) {
        super.visitPostfixExpression(expression)
        if (expression.operationToken == KtTokens.EXCLEXCL) {
            report(CodeSmell(issue, Entity.from(expression), issue.description))
        }
    }
}
