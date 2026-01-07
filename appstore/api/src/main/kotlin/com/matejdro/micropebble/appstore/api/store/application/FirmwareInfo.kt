package com.matejdro.micropebble.appstore.api.store.application

import kotlinx.serialization.Serializable

@Serializable
data class FirmwareInfo(
   val major: Int,
   val minor: Int? = null,
)
