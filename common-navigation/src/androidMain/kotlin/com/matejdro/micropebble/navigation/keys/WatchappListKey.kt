package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.navigation.keys.base.Tab
import com.matejdro.micropebble.navigation.keys.base.TabKey
import com.matejdro.micropebble.navigation.keys.common.InputFile
import kotlinx.serialization.Serializable
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Serializable
data class WatchappListKey(
   val pbwFile: InputFile? = null,
) : ScreenKey(), TabKey {
   override val tab get() = Tab.WATCH_APPS

   override fun getScopeTag(): String {
      return this::class.java.name
   }
}
