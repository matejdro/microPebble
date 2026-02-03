package com.matejdro.micropebble.appstore.api.store.application

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ApplicationIcon(
   @SerialName("28x28")
   val small: String,
   @SerialName("48x48")
   val medium: String,
)
