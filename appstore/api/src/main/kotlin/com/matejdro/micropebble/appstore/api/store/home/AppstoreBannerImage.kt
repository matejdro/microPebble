package com.matejdro.micropebble.appstore.api.store.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppstoreBannerImage(
   @SerialName("720x320")
   val medium: String,
)
