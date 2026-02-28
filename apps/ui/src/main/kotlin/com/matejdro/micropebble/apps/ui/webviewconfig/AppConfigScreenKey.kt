package com.matejdro.micropebble.apps.ui.webviewconfig

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import kotlin.uuid.Uuid

@Serializable
@Immutable
data class AppConfigScreenKey(val uuid: Uuid) : ScreenKey()
