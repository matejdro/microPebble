package com.matejdro.micropebble.navigation.keys.common

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InputFile(val uri: Uri, val filename: String) : Parcelable
