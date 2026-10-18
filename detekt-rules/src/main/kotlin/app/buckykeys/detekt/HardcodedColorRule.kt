package app.buckykeys.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

class HardcodedColorRule(config: Config) : Rule(config) {
    override val issue =
        Issue(
            id = "HardcodedColor",
            severity = Severity.CodeSmell,
            description =
                "Use BuckyTheme.colors.* instead of a hardcoded color. Suppress with " +
                    "@Suppress(\"HardcodedColor\") for legitimate gradient/animation stops.",
            debt = Debt.FIVE_MINS,
        )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text == "Color") {
            val argText = expression.valueArguments.firstOrNull()?.text.orEmpty()
            if (argText.startsWith("0x", ignoreCase = true)) {
                report(CodeSmell(issue, Entity.from(expression), issue.description))
            }
        }
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        if (
            expression.receiverExpression.text == "Color" &&
                expression.selectorExpression?.text in NAMED_COLORS &&
                !isAlphaOverlay(expression)
        ) {
            report(CodeSmell(issue, Entity.from(expression), issue.description))
        }
    }

    private fun isAlphaOverlay(expression: KtDotQualifiedExpression): Boolean {
        val parent = expression.parent as? KtDotQualifiedExpression ?: return false
        val call = parent.selectorExpression as? KtCallExpression ?: return false
        return call.calleeExpression?.text == "copy" &&
            call.valueArguments.any { it.text.contains("alpha") }
    }

    private companion object {
        val NAMED_COLORS = setOf("White", "Black", "Gray", "LightGray", "DarkGray")
    }
}
