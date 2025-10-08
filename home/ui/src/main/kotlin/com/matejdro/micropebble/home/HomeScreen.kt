package com.matejdro.micropebble.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.navigation.keys.DeveloperConnectionScreenKey
import com.matejdro.micropebble.navigation.keys.NotificationAppListKey
import com.matejdro.micropebble.navigation.keys.OnboardingKey
import com.matejdro.micropebble.navigation.keys.WatchListKey
import com.matejdro.micropebble.navigation.keys.WatchappListKey
import com.matejdro.micropebble.navigation.keys.base.HomeScreenKey
import com.matejdro.micropebble.ui.R
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class HomeScreen(
   private val navigator: Navigator,
   private val watchesScreen: Screen<WatchListKey>,
) : Screen<HomeScreenKey>() {
   @Composable
   override fun Content(key: HomeScreenKey) {
      Surface {
         HomeScreenContent(
            { watchesScreen.Content(WatchListKey) },
            { navigator.navigateTo(NotificationAppListKey) },
            { navigator.navigateTo(WatchappListKey) },
            { navigator.navigateTo(OnboardingKey) },
         ) { navigator.navigateTo(DeveloperConnectionScreenKey) }
      }
   }
}

@Composable
private fun HomeScreenContent(
   watchesScreen: @Composable () -> Unit,
   openNotificationApps: () -> Unit,
   openWatchapps: () -> Unit,
   openPermissions: () -> Unit,
   openDevConnection: () -> Unit,
) {
   Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
      FlowRow(
         horizontalArrangement = Arrangement.spacedBy(8.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Button(onClick = openNotificationApps) {
            Text(stringResource(R.string.notification_apps))
         }

         Button(onClick = openWatchapps) {
            Text(stringResource(R.string.watch_apps))
         }

         Button(onClick = openPermissions) {
            Text(stringResource(R.string.permissions))
         }

         Button(onClick = openDevConnection) {
            Text(stringResource(R.string.developer_connection))
         }
      }

      watchesScreen()
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun HomeBlankPreview() {
   PreviewTheme {
      HomeScreenContent(
         watchesScreen = {
            Box(
               Modifier
                  .fillMaxSize()
                  .background(Color.Red)
            )
         },
         openNotificationApps = {},
         openWatchapps = {},
         openPermissions = {}
      ) {}
   }
}
