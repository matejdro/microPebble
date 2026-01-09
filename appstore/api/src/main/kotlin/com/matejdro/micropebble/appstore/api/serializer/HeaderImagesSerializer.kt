package com.matejdro.micropebble.appstore.api.serializer

import com.matejdro.micropebble.appstore.api.store.application.HeaderImage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

/**
 * For some reason some parts of the API use an empty string to represent no items instead of an empty list, making this
 * necessary.
 */
object HeaderImagesSerializer : JsonTransformingSerializer<List<HeaderImage>>(ListSerializer(HeaderImage.serializer())) {
   override fun transformDeserialize(element: JsonElement) =
      if (element is JsonPrimitive && element.isString) JsonArray(emptyList()) else element
}
