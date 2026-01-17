package com.matejdro.micropebble.appstore.api.serializer

import com.matejdro.micropebble.appstore.api.store.application.ApplicationImage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

/**
 * Deserialize the strange string format for companions returned by Algolia.
 */
@OptIn(ExperimentalSerializationApi::class)
object ApplicationImageSerializer : KSerializer<ApplicationImage> {
   override val descriptor: SerialDescriptor
      get() = PrimitiveSerialDescriptor(
         "com.matejdro.micropebble.appstore.api.serializer.ApplicationImageSerializer", PrimitiveKind.STRING
      )

   override fun serialize(encoder: Encoder, value: ApplicationImage) {
      encoder.encodeSerializableValue(ApplicationImage.serializer(), value)
   }

   override fun deserialize(decoder: Decoder): ApplicationImage {
      return (decoder as? JsonDecoder ?: throw SerializationException("Only JSON is supported")).decodeJsonElement().let {
         if (it is JsonPrimitive) {
            ApplicationImage(small = it.content, medium = it.content.replace("144x144", "80x80") /* cursed */)
         } else {
            json.decodeFromJsonElement(ApplicationImage.serializer(), it)
         }
      }
   }
}
