package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.navigation.keys.base.Tab
import com.matejdro.micropebble.navigation.keys.base.TabKey
import kotlinx.serialization.Serializable
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Serializable
data object NotificationAppListKey : ScreenKey(), TabKey {
   override val tab get() = Tab.NOTIFICATIONS
}
