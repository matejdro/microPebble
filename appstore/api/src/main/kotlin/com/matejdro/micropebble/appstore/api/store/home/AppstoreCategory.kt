package com.matejdro.micropebble.appstore.api.store.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
