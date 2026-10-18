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

class TimberErrorArgumentRule(config: Config) : Rule(config) {
    override val issue =
        Issue(
            id = "TimberErrorArgument",
            severity = Severity.Defect,
            description =
                "Use the two-arg Timber.e(throwable, message) form so Sentry/Crashlytics receive the " +
                    "stack trace. Pass a synthesized IllegalStateException when no throwable is available.",
            debt = Debt.FIVE_MINS,
        )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val receiver =
            (expression.parent as? KtDotQualifiedExpression)?.receiverExpression?.text ?: return
        if (
            receiver == "Timber" &&
                expression.calleeExpression?.text == "e" &&
                expression.valueArguments.size == 1
        ) {
            report(CodeSmell(issue, Entity.from(expression), issue.description))
        }
    }
}
