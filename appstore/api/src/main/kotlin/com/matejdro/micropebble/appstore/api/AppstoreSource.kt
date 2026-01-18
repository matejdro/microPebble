package com.matejdro.micropebble.appstore.api

import kotlinx.serialization.Serializable

@Serializable
data class AppstoreSource(
   val url: String,
   val name: String,

   val algoliaData: AlgoliaData? = null,

   val enabled: Boolean = true,
)
