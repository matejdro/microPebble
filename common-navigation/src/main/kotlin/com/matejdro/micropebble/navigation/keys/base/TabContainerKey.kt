package com.matejdro.micropebble.navigation.keys.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

/**
 * Key for a screen that shows a list of tabs + container. Container will be filled from the next entry on the backstack.
 */
interface TabContainerKey

val LocalSelectedTabContent = staticCompositionLocalOf<SelectedTabContent> { error("SelectedTabContent not provided") }

data class SelectedTabContent(val content: @Composable () -> Unit, val key: ScreenKey)
