package com.matejdro.micropebble.navigation.scenes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.matejdro.micropebble.navigation.keys.base.LocalSelectedTabContent
import com.matejdro.micropebble.navigation.keys.base.SelectedTabContent
import com.matejdro.micropebble.navigation.keys.base.TabContainerKey
import si.inova.kotlinova.navigation.navigation3.key
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

class TabListScene(
   override val key: NavEntry<ScreenKey>,
   override val entries: List<NavEntry<ScreenKey>>,
   override val previousEntries: List<NavEntry<ScreenKey>>,
   val displayedEntry: NavEntry<ScreenKey>,
) : Scene<ScreenKey> {
   override val content: @Composable (() -> Unit) = {
      val tabContainerEntry = entries.first()

      val selectedTabContent = SelectedTabContent(displayedEntry::Content, displayedEntry.key())
      CompositionLocalProvider(LocalSelectedTabContent provides selectedTabContent) {
         tabContainerEntry.Content()
      }
   }
}

@Composable
fun rememberTabListSceneDecoratorStrategy(): SceneStrategy<ScreenKey> {
   return remember() {
      TabListDetailSceneDecoratorStrategy()
   }
}

private class TabListDetailSceneDecoratorStrategy : SceneStrategy<ScreenKey> {
   override fun SceneStrategyScope<ScreenKey>.calculateScene(
      entries: List<NavEntry<ScreenKey>>,
   ): Scene<ScreenKey>? {
      val secondToLastKey = entries.elementAtOrNull(entries.size - 2)

      return if (secondToLastKey?.key() is TabContainerKey) {
         val lastKey = entries.last()

         TabListScene(
            key = secondToLastKey,
            entries = listOf(secondToLastKey, lastKey),
            previousEntries = entries.dropLast(2),
            displayedEntry = lastKey
         )
      } else {
         null
      }
   }
}
