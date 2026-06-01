package com.matejdro.micropebble.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.navigation.instructions.ReplaceTabContentWith
import com.matejdro.micropebble.navigation.keys.HomeScreenKey
import com.matejdro.micropebble.navigation.keys.NotificationAppListKey
import com.matejdro.micropebble.navigation.keys.WatchListKey
import com.matejdro.micropebble.navigation.keys.WatchappListKey
import com.matejdro.micropebble.navigation.keys.base.LocalSelectedTabContent
import com.matejdro.micropebble.navigation.keys.base.SelectedTabContent
import com.matejdro.micropebble.navigation.keys.base.Tab
import com.matejdro.micropebble.tools.ToolsScreenKey
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import si.inova.kotlinova.core.activity.requireActivity
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class HomeScreen(
   private val navigator: Navigator,
) : Screen<HomeScreenKey>() {
   @Composable
   override fun Content(key: HomeScreenKey) {
      val sizeClass = calculateWindowSizeClass(activity = LocalContext.current.requireActivity())

      Surface {
         HomeScreenContent(
            selectedContent = LocalSelectedTabContent.current,
            tabletMode = sizeClass.widthSizeClass == WindowWidthSizeClass.Expanded,
            switchTab = { navigator.navigate(ReplaceTabContentWith(it.toScreenKey())) },
         )
      }
   }
}

private fun Tab.toScreenKey(): ScreenKey = when (this) {
   Tab.WATCHES -> WatchListKey
   Tab.WATCH_APPS -> WatchappListKey()
   Tab.NOTIFICATIONS -> NotificationAppListKey
   Tab.TOOLS -> ToolsScreenKey
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun HomePhonePreview() {
   PreviewTheme {
      HomeScreenContent(
         selectedContent = SelectedTabContent(
            {
               Box(
                  Modifier
                     .fillMaxSize()
                     .background(Color.Red)
               )
            },
            Tab.WATCH_APPS,
            ""
         ),
         tabletMode = false,
         switchTab = {},
      )
   }
}

@Preview(device = Devices.TABLET)
@Composable
@ShowkaseComposable(group = "Test")
internal fun HomeTabletPreview() {
   PreviewTheme {
      HomeScreenContent(
         selectedContent = SelectedTabContent(
            {
               Box(
                  Modifier
                     .fillMaxSize()
                     .background(Color.Red)
               )
            },
            Tab.WATCH_APPS,
            ""
         ),
         tabletMode = true,
         switchTab = {},
      )
   }
}
