package com.matejdro.micropebble.appstore.api.serializer

import com.matejdro.micropebble.appstore.api.store.application.HeaderImage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

/**
 * For some reason some parts of the API use an empty string to represent no items instead of an empty list, making this
 * necessary.
 */
object HeaderImagesSerializer : KSerializer<List<HeaderImage>> {
   private val listSerializer = ListSerializer(HeaderImage.serializer())

   override val descriptor: SerialDescriptor get() = listSerializer.descriptor

   override fun serialize(encoder: Encoder, value: List<HeaderImage>) {
      listSerializer.serialize(encoder, value)
   }

   override fun deserialize(decoder: Decoder): List<HeaderImage> {
      return if (decoder is JsonDecoder) {
         val element = decoder.decodeJsonElement()
         val decodedElement = if (element is JsonPrimitive && element.isString) JsonArray(emptyList()) else element
         json.decodeFromJsonElement(listSerializer, decodedElement)
      } else {
         listSerializer.deserialize(decoder)
      }
   }
}
