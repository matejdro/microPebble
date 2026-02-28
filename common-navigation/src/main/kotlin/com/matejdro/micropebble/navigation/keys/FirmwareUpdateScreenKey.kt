package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.navigation.keys.common.InputFile
import kotlinx.serialization.Serializable
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Serializable
data class FirmwareUpdateScreenKey(
   val watchSerial: String? = null,
   val pbzFile: InputFile? = null,
) : ScreenKey()
