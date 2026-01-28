package com.matejdro.micropebble.appstore.api

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class AppstoreSource(
   val id: Uuid,
   val url: String,
   val name: String,

   val algoliaData: AlgoliaData? = null,

   val enabled: Boolean = true,
)
