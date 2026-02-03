package com.matejdro.micropebble.appstore.api.store.application

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ApplicationCompanion(
   val id: Int,
   val name: String,
   val url: String,
   val icon: String,
   @SerialName("pebblekit_version")
   val pebblekitVersion: String,
   val required: Boolean,
)
