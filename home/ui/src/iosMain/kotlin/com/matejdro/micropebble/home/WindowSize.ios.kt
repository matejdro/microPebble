package com.matejdro.micropebble.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

// Material3 "Expanded" starts at 840dp wide.
private val EXPANDED_WIDTH_BREAKPOINT = 840.dp

@Composable
actual fun isExpandedWidth(): Boolean {
   val containerSize = LocalWindowInfo.current.containerSize
   val widthDp = with(LocalDensity.current) { containerSize.width.toDp() }
   return widthDp >= EXPANDED_WIDTH_BREAKPOINT
}
