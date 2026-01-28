package com.matejdro.micropebble.appstore.api.store.application

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeaderImage(
   @SerialName("720x320")
   val medium: String,
   @SerialName("orig")
   val original: String,
)
