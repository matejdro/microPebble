package com.matejdro.micropebble.tools

import com.matejdro.micropebble.navigation.keys.base.Tab
import com.matejdro.micropebble.navigation.keys.base.TabKey
import kotlinx.serialization.Serializable
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Serializable
data object ToolsScreenKey : ScreenKey(), TabKey {
   override val tab get() = Tab.TOOLS
}
