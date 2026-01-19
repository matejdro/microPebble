package com.matejdro.micropebble.appstore.api

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class AppInstallSource(
   val appId: Uuid,
   val storeId: String,
   val sourceId: Uuid,
)
