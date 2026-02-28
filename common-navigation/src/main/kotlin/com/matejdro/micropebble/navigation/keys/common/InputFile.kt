package com.matejdro.micropebble.navigation.keys.common

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class InputFile(
   @Serializable(with = UriAsStringSerializer::class)
   val uri: Uri,
   val filename: String,
)

object UriAsStringSerializer : KSerializer<Uri> {
   // Serial names of descriptors should be unique, this is why we advise including app package in the name.
   override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("android.net.Uri", PrimitiveKind.STRING)

   override fun serialize(encoder: Encoder, value: Uri) {
      val string = value.toString()
      encoder.encodeString(string)
   }

   override fun deserialize(decoder: Decoder): Uri {
      val string = decoder.decodeString()
      return string.toUri()
   }
}
