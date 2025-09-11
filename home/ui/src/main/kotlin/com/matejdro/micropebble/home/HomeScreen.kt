package com.matejdro.micropebble.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.matejdro.micropebble.navigation.keys.base.BluetoothScanScreenKey
import com.matejdro.micropebble.navigation.keys.base.HomeScreenKey
import com.matejdro.micropebble.ui.R
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class HomeScreen(
   private val navigator: Navigator,
) : Screen<HomeScreenKey>() {
   @Composable
   override fun Content(key: HomeScreenKey) {
      Surface(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
         Column {
            Button(onClick = { navigator.navigateTo(BluetoothScanScreenKey) }) {
               Text(stringResource(R.string.pair_new_watch))
            }
         }
      }
   }
}
