package com.matejdro.micropebble.appstore.api.store.home

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AppstoreBannerImage(
   @SerialName("720x320")
   val medium: String,
)
