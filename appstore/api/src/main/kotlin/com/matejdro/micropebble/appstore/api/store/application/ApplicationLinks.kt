package com.matejdro.micropebble.appstore.api.store.application

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationLinks(
   val add: String,
   @SerialName("add_flag")
   val addFlag: String,
   @SerialName("add_heart")
   val addHeart: String,
   val remove: String,
   @SerialName("remove_flag")
   val removeFlag: String,
   @SerialName("remove_heart")
   val removeHeart: String,
   val share: String,
)
