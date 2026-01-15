package com.matejdro.micropebble.appstore.api.store.application

import kotlinx.serialization.SerialName

enum class ApplicationType(val apiEndpoint: String) {
   @SerialName("watchface")
   Watchface(apiEndpoint = "/api/v1/home/faces?platform=all"),

   @SerialName("watchapp")
   Watchapp(apiEndpoint = "/api/v1/home/apps?platform=all"),
}
