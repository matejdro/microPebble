package com.matejdro.micropebble.appstore.api

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class AppInstallSource(
   /**
    * The UUID of the app itself, as specified by its package.json.
    */
   val appId: Uuid,

   /**
    * The ID of the app in the store, as specified by the web services API.
    */
   val storeId: String,

   /**
    * The UUID of the source this app was installed from.
    */
   val sourceId: Uuid,
)
