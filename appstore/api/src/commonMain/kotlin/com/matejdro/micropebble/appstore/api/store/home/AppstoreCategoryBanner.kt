package com.matejdro.micropebble.appstore.api.store.home

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AppstoreCategoryBanner(
   @SerialName("application_id")
   val appId: String,
   val title: String,
   val image: AppstoreBannerImage,
)
