package com.matejdro.micropebble.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class CustomRuleProvider : RuleSetProvider {
   override val ruleSetId: String
      get() = "custom"

   override fun instance(config: Config): RuleSet {
      return RuleSet(
         ruleSetId,
         listOf(
            UseActionLoggerInViewModels(config)
         )
      )
   }
}
