package com.matejdro.micropebble.navigation.keys.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

enum class Tab {
   WATCHES,
   WATCH_APPS,
   NOTIFICATIONS,
   TOOLS,
}

/** Implemented by screen keys that act as a top-level tab destination. */
interface TabKey {
   val tab: Tab
}

val LocalSelectedTabContent = staticCompositionLocalOf<SelectedTabContent> { error("SelectedTabContent not provided") }

/** Content shown inside a tab container, plus which [Tab] is currently selected (null when none is). */
data class SelectedTabContent(val content: @Composable () -> Unit, val tab: Tab?, val contentKey: Any)
