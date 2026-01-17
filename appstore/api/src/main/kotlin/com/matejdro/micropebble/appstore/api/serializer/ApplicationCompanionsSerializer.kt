package com.matejdro.micropebble.appstore.api.serializer

import com.matejdro.micropebble.appstore.api.store.application.ApplicationCompanions
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
object ApplicationCompanionsSerializer : KSerializer<ApplicationCompanions> {
   override val descriptor: SerialDescriptor
      get() = PrimitiveSerialDescriptor(
         "com.matejdro.micropebble.appstore.api.serializer.ApplicationCompanionsSerializer", PrimitiveKind.STRING
      )

   override fun serialize(encoder: Encoder, value: ApplicationCompanions) {
      encoder.encodeSerializableValue(ApplicationCompanions.serializer(), value)
   }

   override fun deserialize(decoder: Decoder): ApplicationCompanions {
      return (decoder as? JsonDecoder ?: throw SerializationException("Only JSON is supported")).decodeJsonElement().let {
         if (it is JsonPrimitive) {
            ApplicationCompanions(null, null)
         } else {
            json.decodeFromJsonElement(ApplicationCompanions.serializer(), it)
         }
      }
   }
}
