package com.matejdro.micropebble.apps.ui.webviewconfig

import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import kotlin.uuid.Uuid

@Parcelize
@Immutable
data class AppConfigScreenKey(val uuid: Uuid) : ScreenKey()
