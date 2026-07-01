package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.navigation.keys.common.InputFile
import kotlinx.serialization.Serializable
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Serializable
data class WatchappListKey(
   val pbwFile: InputFile? = null,
) : ScreenKey() {
   override fun getScopeTag(): String {
      return this::class.java.name
   }
}
