package com.matejdro.micropebble.appstore.api

import kotlinx.serialization.Serializable

@Serializable
data class AlgoliaData(
   val appId: String,
   val apiKey: String,
   val indexName: String,
)
