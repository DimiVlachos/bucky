package app.buckykeys.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class BuckyRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "bucky"

    override fun instance(config: Config): RuleSet =
        RuleSet(
            ruleSetId,
            listOf(
                HardcodedColorRule(config),
                NoNotNullAssertionRule(config),
                CatchBlockHygieneRule(config),
                TimberErrorArgumentRule(config),
            ),
        )
}
