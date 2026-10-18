package dev.pckeys.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class PcKeysRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "pckeys"

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
