package com.matejdro.micropebble.ios

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
   MaterialTheme {
      Surface(modifier = Modifier.fillMaxSize()) {
         Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("microPebble — running on iOS")
         }
      }
   }
}
