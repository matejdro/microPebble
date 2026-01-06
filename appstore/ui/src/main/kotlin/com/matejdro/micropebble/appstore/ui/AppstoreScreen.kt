package com.matejdro.micropebble.appstore.ui

import androidx.compose.runtime.Composable
import si.inova.kotlinova.navigation.screens.Screen
import androidx.compose.material3.Text
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen

@InjectNavigationScreen
class AppstoreScreen : Screen<AppstoreScreenKey>() {
   @Composable
   override fun Content(key: AppstoreScreenKey) {
      Text("Appstore screen")
   }
}
