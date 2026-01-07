package com.matejdro.micropebble.appstore.api.store.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppstoreCategoryIcon(
   @SerialName("88x88")
   val medium: String? = null,
)
