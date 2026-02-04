package com.matejdro.micropebble.appstore.api.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes blank (consists solely of whitespace; see [CharSequence.isBlank]) strings as null.
 */
@OptIn(ExperimentalSerializationApi::class)
object BlankStringSerializer : KSerializer<String?> {
   override val descriptor: SerialDescriptor
      get() = PrimitiveSerialDescriptor(
         "com.matejdro.micropebble.appstore.api.serializer.BlankStringSerializer", PrimitiveKind.STRING
      )

   override fun serialize(encoder: Encoder, value: String?) {
      if (value.isNullOrBlank()) {
         encoder.encodeNull()
      } else {
         encoder.encodeString(value)
      }
   }

   override fun deserialize(decoder: Decoder) = if (decoder.decodeNotNullMark()) decoder.decodeString().ifBlank { null } else null
}
