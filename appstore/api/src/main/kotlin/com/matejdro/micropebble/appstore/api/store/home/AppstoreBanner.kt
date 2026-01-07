package com.matejdro.micropebble.appstore.api.store.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppstoreBanner(
   @SerialName("application_id")
   val appId: String,
   val title: String,
   val image: String,
)
