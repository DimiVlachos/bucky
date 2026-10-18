package dev.pckeys.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

class CatchBlockHygieneRule(config: Config) : Rule(config) {
    override val issue =
        Issue(
            id = "CatchBlockHygiene",
            severity = Severity.Defect,
            description =
                "A catch block in coroutine code must let CancellationException propagate — a " +
                    "preceding `catch (e: CancellationException) { throw e }` clause, an " +
                    "`if (e is CancellationException) throw e` guard as the first statement, or " +
                    "`ensureActive()` as the first statement.",
            debt = Debt.FIVE_MINS,
        )

    override fun visitCatchSection(catchClause: KtCatchClause) {
        super.visitCatchSection(catchClause)
        if (!isInCoroutineCode(catchClause)) return
        val typeName = catchClause.simpleTypeName() ?: return
        if (typeName == CANCELLATION) {
            if (!rethrowsFirst(catchClause)) report(catchClause)
            return
        }
        if (typeName !in SWALLOWING_TYPES) return
        val tryExpression = catchClause.parent as? KtTryExpression
        val clauses = tryExpression?.catchClauses.orEmpty()
        val index = clauses.indexOf(catchClause)
        val precedingClauseRethrows =
            clauses.take(index).any { it.simpleTypeName() == CANCELLATION && rethrowsFirst(it) }
        if (precedingClauseRethrows) return
        if (guardsFirst(catchClause)) return
        report(catchClause)
    }

    private fun report(catchClause: KtCatchClause) {
        report(CodeSmell(issue, Entity.from(catchClause), issue.description))
    }

    private fun isInCoroutineCode(catchClause: KtCatchClause): Boolean {
        var node = catchClause.parent
        while (node != null) {
            when (node) {
                is KtNamedFunction -> return node.hasModifier(KtTokens.SUSPEND_KEYWORD)
                is KtLambdaExpression -> if (isCoroutineBuilderLambda(node)) return true
            }
            node = node.parent
        }
        return false
    }

    private fun isCoroutineBuilderLambda(lambda: KtLambdaExpression): Boolean {
        val call =
            when (val holder = lambda.parent) {
                is KtLambdaArgument -> holder.parent as? KtCallExpression
                is KtValueArgument -> holder.getStrictParentOfType<KtCallExpression>()
                else -> null
            } ?: return false
        return call.calleeExpression?.text in COROUTINE_BUILDERS
    }

    private fun KtCatchClause.simpleTypeName(): String? =
        catchParameter?.typeReference?.text?.substringAfterLast('.')

    private fun KtCatchClause.firstStatement(): KtExpression? =
        (catchBody as? KtBlockExpression)?.statements?.firstOrNull() ?: catchBody

    private fun rethrowsFirst(catchClause: KtCatchClause): Boolean =
        catchClause.catchBody?.endsInThrow() == true

    private fun guardsFirst(catchClause: KtCatchClause): Boolean {
        val parameterName = catchClause.catchParameter?.name ?: return false
        return when (val first = catchClause.firstStatement()) {
            is KtIfExpression -> first.isCancellationRethrow(parameterName)
            is KtDotQualifiedExpression -> first.isEnsureActive()
            is KtCallExpression -> first.isEnsureActive()
            else -> false
        }
    }

    private fun KtIfExpression.isCancellationRethrow(parameterName: String): Boolean {
        val condition = condition?.leftmostConjunct() as? KtIsExpression ?: return false
        val subject = condition.leftHandSide as? KtNameReferenceExpression ?: return false
        val typeMatches =
            condition.typeReference?.text?.substringAfterLast('.') == CANCELLATION &&
                !condition.isNegated
        val thenThrows = `then`?.endsInThrow() == true
        return subject.getReferencedName() == parameterName && typeMatches && thenThrows
    }

    private fun KtExpression.leftmostConjunct(): KtExpression {
        var node: KtExpression = this
        while (node is KtBinaryExpression && node.operationToken == KtTokens.ANDAND) {
            node = node.left ?: return node
        }
        return node
    }

    private fun KtExpression.endsInThrow(): Boolean =
        ((this as? KtBlockExpression)?.statements?.lastOrNull() ?: this) is KtThrowExpression

    private fun KtExpression.isEnsureActive(): Boolean {
        val call =
            when (this) {
                is KtDotQualifiedExpression -> selectorExpression as? KtCallExpression
                is KtCallExpression -> this
                else -> null
            }
        return call?.calleeExpression?.text == "ensureActive"
    }

    private companion object {
        const val CANCELLATION = "CancellationException"
        val SWALLOWING_TYPES =
            setOf("Exception", "Throwable", "RuntimeException", "IllegalStateException")
        val COROUTINE_BUILDERS =
            setOf(
                "launch",
                "async",
                "withContext",
                "coroutineScope",
                "supervisorScope",
                "runBlocking",
                "withTimeout",
                "withTimeoutOrNull",
                "withLock",
                "withPermit",
                "select",
                "repeatOnLifecycle",
                "LaunchedEffect",
                "produceState",
                "pointerInput",
                "awaitPointerEventScope",
                "flow",
                "channelFlow",
                "callbackFlow",
                "collect",
                "collectLatest",
                "collectIndexed",
                "mapLatest",
                "onStart",
                "onCompletion",
                "transform",
                "transformLatest",
                "flatMapLatest",
                "flatMapConcat",
                "flatMapMerge",
                "retry",
                "retryWhen",
                "catch",
                "produce",
            )
    }
}
