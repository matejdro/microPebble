package com.matejdro.micropebble.navigation.keys

import android.os.Parcelable
import com.matejdro.micropebble.navigation.keys.base.BaseSingleTopScreenKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeScreenKey(val selectedScreen: Screen) : BaseSingleTopScreenKey() {
   @Parcelize
   sealed class Screen : Parcelable {
      data object WATCHES : Screen()
      data object APPS : Screen()
      data object NOTIFICATIONS : Screen()
      data object TOOLS : Screen()
   }
}
