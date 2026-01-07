package com.matejdro.micropebble.appstore.api.store.application

import kotlinx.serialization.SerialName

enum class ApplicationType {
   @SerialName("watchface")
   Watchface,

   @SerialName("watchapp")
   Watchapp,
}
