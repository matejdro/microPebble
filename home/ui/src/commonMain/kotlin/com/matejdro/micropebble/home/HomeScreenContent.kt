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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.matejdro.micropebble.home.resources.Res
import com.matejdro.micropebble.home.resources.notifications
import com.matejdro.micropebble.home.resources.tools
import com.matejdro.micropebble.home.resources.watch_apps
import com.matejdro.micropebble.home.resources.watchapps
import com.matejdro.micropebble.home.resources.watches
import com.matejdro.micropebble.navigation.keys.base.SelectedTabContent
import com.matejdro.micropebble.navigation.keys.base.Tab
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreenContent(
   selectedContent: SelectedTabContent,
   switchTab: (Tab) -> Unit,
   tabletMode: Boolean = isExpandedWidth(),
) {
   val tabs = homeTabs()
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
private fun homeTabs(): List<HomeTab> {
   val watchesIcon = painterResource(Res.drawable.watches)
   val watchAppsIcon = painterResource(Res.drawable.watchapps)
   val notificationsIcon = painterResource(Res.drawable.notifications)
   val toolsIcon = painterResource(Res.drawable.tools)
   val watchesLabel = stringResource(Res.string.watches)
   val watchAppsLabel = stringResource(Res.string.watch_apps)
   val notificationsLabel = stringResource(Res.string.notifications)
   val toolsLabel = stringResource(Res.string.tools)

   return remember(
      watchesIcon, watchAppsIcon, notificationsIcon, toolsIcon,
      watchesLabel, watchAppsLabel, notificationsLabel, toolsLabel,
   ) {
      listOf(
         HomeTab(Tab.WATCHES, watchesIcon, watchesLabel),
         HomeTab(Tab.WATCH_APPS, watchAppsIcon, watchAppsLabel),
         HomeTab(Tab.NOTIFICATIONS, notificationsIcon, notificationsLabel),
         HomeTab(Tab.TOOLS, toolsIcon, toolsLabel),
      )
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
