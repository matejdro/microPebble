package com.matejdro.micropebble.appstore.api.serializer

import com.matejdro.micropebble.appstore.api.store.application.HeaderImage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

object HeaderImagesSerializer : JsonTransformingSerializer<List<HeaderImage>>(ListSerializer(HeaderImage.serializer())) {
   override fun transformDeserialize(element: JsonElement) =
      if (element is JsonPrimitive && element.isString) JsonArray(emptyList()) else element
}
