package com.matejdro.micropebble.appstore.api.serializer

import com.matejdro.micropebble.appstore.api.store.application.ApplicationIcon
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
object ApplicationIconSerializer : KSerializer<ApplicationIcon> {
   override val descriptor: SerialDescriptor
      get() = PrimitiveSerialDescriptor(
         "com.matejdro.micropebble.appstore.api.serializer.ApplicationIconSerializer", PrimitiveKind.STRING
      )

   override fun serialize(encoder: Encoder, value: ApplicationIcon) {
      encoder.encodeSerializableValue(ApplicationIcon.serializer(), value)
   }

   override fun deserialize(decoder: Decoder): ApplicationIcon {
      return (decoder as? JsonDecoder ?: throw SerializationException("Only JSON is supported")).decodeJsonElement().let {
         if (it is JsonPrimitive) {
            ApplicationIcon(small = it.content.replace("48x48", "28x28") /* cursed */, medium = it.content)
         } else {
            json.decodeFromJsonElement(ApplicationIcon.serializer(), it)
         }
      }
   }
}
