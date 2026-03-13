package com.matejdro.micropebble.appstore.api.store.home

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AppstoreCategoryIcon(
   @SerialName("88x88")
   val medium: String? = null,
)
