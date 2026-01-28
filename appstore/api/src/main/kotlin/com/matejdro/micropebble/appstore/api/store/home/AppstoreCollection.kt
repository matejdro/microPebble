package com.matejdro.micropebble.appstore.api.store.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppstoreCollection(
   @SerialName("application_ids")
   val appIds: List<String>,
   val links: AppstoreLinks,
   val name: String,
   val slug: String,
)
