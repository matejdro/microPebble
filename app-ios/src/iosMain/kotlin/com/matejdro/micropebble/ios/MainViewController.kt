package com.matejdro.micropebble.ios

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.matejdro.micropebble.home.HomeScreenContent
import com.matejdro.micropebble.navigation.keys.base.SelectedTabContent
import com.matejdro.micropebble.navigation.keys.base.Tab
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
   MaterialTheme {
      Surface {
         var selectedTab by remember { mutableStateOf(Tab.WATCHES) }

         // Snapshot the tab so each SelectedTabContent renders its own tab; otherwise the
         // outgoing pane of the cross-fade would re-read the live state and show the new tab.
         val tab = selectedTab
         HomeScreenContent(
            selectedContent = SelectedTabContent(
               content = { TabPlaceholder(tab) },
               tab = tab,
               contentKey = tab,
            ),
            switchTab = { selectedTab = it },
         )
      }
   }
}

@Composable
private fun TabPlaceholder(tab: Tab) {
   Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(tab.name)
   }
}
