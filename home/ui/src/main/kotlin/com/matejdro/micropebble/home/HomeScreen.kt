package com.matejdro.micropebble.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.matejdro.micropebble.navigation.keys.base.HomeScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class HomeScreen : Screen<HomeScreenKey>() {
   @Composable
   override fun Content(key: HomeScreenKey) {
      Surface(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
         Text("Hello World")
      }
   }
}
