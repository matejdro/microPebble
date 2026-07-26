package com.matejdro.micropebble.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.matejdro.micropebble.navigation.keys.base.SelectedTabContent
import com.matejdro.micropebble.navigation.keys.base.Tab

@Composable
fun HomeScreenContent(
   tabs: List<HomeTab>,
   selectedContent: SelectedTabContent,
   tabletMode: Boolean,
   switchTab: (Tab) -> Unit,
) {
   val animatedMainContent: @Composable () -> Unit = {
      AnimatedContent(
         selectedContent,
         contentKey = { entry -> entry.contentKey },
         transitionSpec = { fadeIn() togetherWith fadeOut() }
      ) {
         it.content()
      }
   }
   if (tabletMode) {
      NavigationRailContent(animatedMainContent, tabs, selectedContent.tab, switchTab)
   } else {
      NavigationBarContent(animatedMainContent, tabs, selectedContent.tab, switchTab)
   }
}

@Composable
private fun NavigationBarContent(
   mainContent: @Composable () -> Unit,
   tabs: List<HomeTab>,
   selectedTab: Tab?,
   switchTab: (Tab) -> Unit,
) {
   Column {
      Box(
         Modifier
            .fillMaxWidth()
            .weight(1f)
      ) {
         mainContent()
      }

      NavigationBar {
         tabs.forEach { tab ->
            NavigationBarItem(
               selected = selectedTab == tab.tab,
               onClick = { switchTab(tab.tab) },
               icon = { Icon(painter = tab.icon, contentDescription = null) },
               label = { Text(tab.label) }
            )
         }
      }
   }
}

@Composable
private fun NavigationRailContent(
   mainContent: @Composable () -> Unit,
   tabs: List<HomeTab>,
   selectedTab: Tab?,
   switchTab: (Tab) -> Unit,
) {
   Row {
      NavigationRail {
         tabs.forEach { tab ->
            NavigationRailItem(
               selected = selectedTab == tab.tab,
               onClick = { switchTab(tab.tab) },
               icon = { Icon(painter = tab.icon, contentDescription = null) },
               label = { Text(tab.label) }
            )
         }
      }

      Box(
         Modifier
            .fillMaxHeight()
            .weight(1f)
      ) {
         mainContent()
      }
   }
}
