package com.matejdro.micropebble.appstore.api.store.application

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class HeaderImage(
   @SerialName("720x320")
   val medium: String,
   @SerialName("orig")
   val original: String,
)
