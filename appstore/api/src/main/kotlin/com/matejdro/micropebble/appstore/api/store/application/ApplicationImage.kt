package com.matejdro.micropebble.appstore.api.store.application

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationImage(
   @SerialName("144x144")
   val small: String,
   @SerialName("80x80")
   val medium: String,
)
