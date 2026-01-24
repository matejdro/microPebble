package com.matejdro.micropebble.appstore.api.store.application

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationScreenshot(
   @SerialName("144x168")
   val medium: String? = null,
   @SerialName("180x180")
   val circle: String? = null,
) {
   enum class Hardware(val aspectRatio: Float, val hardwarePlatforms: List<String>) {
      RECTANGLE(144.0f / 168.0f, listOf("aplite", "basalt", "diorite", "flint")),
      CIRCLE(1.0f, listOf("chalk", "gabbro")),
      RECTANGLE_LARGE(200.0f / 228.0f, listOf("emery")),
      ;

      companion object {
         fun fromHardwarePlatform(platform: String): Hardware? {
            return Hardware.entries.find { platform in it.hardwarePlatforms }
         }
      }
   }

   val image: String? = medium ?: circle
   val imageHardware = if (circle != null) {
      Hardware.CIRCLE
   } else {
      Hardware.RECTANGLE
   }
}
