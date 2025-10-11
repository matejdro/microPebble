package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.navigation.keys.base.BaseSingleTopScreenKey
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Parcelize
data class HomeScreenKey(val selectedScreen: ScreenKey) : BaseSingleTopScreenKey()
