package com.matejdro.micropebble.navigation.keys

import android.net.Uri
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Parcelize
data class FirmwareUpdateScreenKey(
   val watchSerial: String? = null,
   val pbzUri: Uri? = null,
) : ScreenKey()
