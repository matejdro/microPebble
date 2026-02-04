package com.matejdro.micropebble.appstore.api.store.home

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AppstoreCategory(
   @SerialName("application_ids")
   val appIds: List<String>,
   val banners: List<AppstoreCategoryBanner>,
   val color: String,
   val icon: AppstoreCategoryIcon,
   val id: String,
   val links: AppstoreCategoryLinks,
   val name: String,
   val slug: String,
)
