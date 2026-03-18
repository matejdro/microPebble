package com.matejdro.micropebble.appstore.api.store.application

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class FirmwareInfo(
   val major: Int,
   val minor: Int? = null,
)
