package com.matejdro.micropebble.home

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import si.inova.kotlinova.core.activity.requireActivity

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun isExpandedWidth(): Boolean {
   val sizeClass = calculateWindowSizeClass(activity = LocalContext.current.requireActivity())
   return sizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
}
