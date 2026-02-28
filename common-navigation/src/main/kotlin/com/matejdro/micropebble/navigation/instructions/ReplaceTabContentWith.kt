package com.matejdro.micropebble.navigation.instructions

import com.matejdro.micropebble.navigation.keys.base.TabContainerKey
import com.zhuinden.simplestack.StateChange
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Parcelize
data class ReplaceTabContentWith(val key: ScreenKey) : NavigationInstruction() {
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
      return NavigationResult(backstackUntilHomeScreen + key, StateChange.REPLACE)
   }
}
