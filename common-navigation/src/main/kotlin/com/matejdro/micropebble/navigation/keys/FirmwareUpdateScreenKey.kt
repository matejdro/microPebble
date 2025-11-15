package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.navigation.keys.common.InputFile
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Parcelize
data class FirmwareUpdateScreenKey(
   val watchSerial: String? = null,
   val pbzFile: InputFile? = null,
) : ScreenKey()
