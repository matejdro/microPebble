package com.matejdro.micropebble.navigation.instructions

import com.matejdro.micropebble.navigation.keys.base.TabContainerKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Serializable
data class ReplaceTabContentWith(val key: @Contextual ScreenKey) : NavigationInstruction() {
   override fun performNavigation(
      backstack: List<ScreenKey>,
      context: NavigationContext,
   ): NavigationResult {
      val lastHomeScreenKey = backstack.indexOfLast { it is TabContainerKey }

      val backstackUntilHomeScreen = if (lastHomeScreenKey < 0) {
         backstack
      } else {
         backstack.take(lastHomeScreenKey + 1)
      }
      return NavigationResult(backstackUntilHomeScreen + key)
   }
}
