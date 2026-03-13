package com.matejdro.micropebble.appstore.api.store.application

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class CompatibilityInfo(
   val supported: Boolean,
   val firmware: FirmwareInfo? = null,
   @SerialName("min_js_version")
   val minJsVersion: Int? = null,
)
