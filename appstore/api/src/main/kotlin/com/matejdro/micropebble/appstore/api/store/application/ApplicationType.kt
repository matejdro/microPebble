package com.matejdro.micropebble.appstore.api.store.application

import kotlinx.serialization.SerialName

enum class ApplicationType(val apiEndpoint: String, val searchTag: String) {
   @SerialName("watchface")
   Watchface(apiEndpoint = "/v1/home/faces?platform=all", searchTag = "watchface"),

   @SerialName("watchapp")
   Watchapp(apiEndpoint = "/v1/home/apps?platform=all", searchTag = "watchapp"),
}
