package com.matejdro.micropebble.detekt

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class CustomRuleProvider : RuleSetProvider {
   override val ruleSetId = RuleSetId("custom")

   override fun instance(): RuleSet {
      return RuleSet(
         ruleSetId,
         listOf(
            ::UseActionLoggerInViewModels
         )
      )
   }
}
