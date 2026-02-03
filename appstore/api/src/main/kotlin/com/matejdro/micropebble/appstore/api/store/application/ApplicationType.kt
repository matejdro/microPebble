package com.matejdro.micropebble.appstore.api.store.application

import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Immutable
@Serializable(with = ApplicationType.Serializer::class)
sealed class ApplicationType(val ordinal: Int, val apiEndpoint: String, val searchTag: String) {
   @SerialName("watchface")
   data object Watchface : ApplicationType(ordinal = 0, apiEndpoint = "/v1/home/faces", searchTag = "watchface")

   @SerialName("watchapp")
   data object Watchapp : ApplicationType(ordinal = 1, apiEndpoint = "/v1/home/apps", searchTag = "watchapp")

   object Serializer : KSerializer<ApplicationType> {
      override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
         "com.matejdro.micropebble.appstore.api.store.application.ApplicationTypeSerializer",
         PrimitiveKind.STRING
      )

      override fun serialize(encoder: Encoder, value: ApplicationType) = encoder.encodeString(value.searchTag)

      override fun deserialize(decoder: Decoder) = when (decoder.decodeString()) {
         "watchface" -> Watchface
         "watchapp" -> Watchapp
         else -> throw SerializationException()
      }
   }
}
