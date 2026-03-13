package com.matejdro.micropebble.appstore.api.serializer

import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * The first one that doesn't throw goes
 */
private val parsers: List<(String) -> Instant> = listOf(
   {
      Instant.parse(it)
   },
   {
      DateTimeComponents.Formats.RFC_1123.parse(it).toInstantUsingOffset()
   },
   {
      LocalDateTime.parse(it).toInstant(TimeZone.UTC)
   },
)

private fun tryAllOfThem(input: String): Instant {
   for (parser in parsers) {
      return runCatching { parser(input) }.getOrNull() ?: continue
   }
   error("All parsers failed \uD83D\uDE2D\uD83D\uDE2D")
}

/**
 * Serializes an [Instant] with [Instant.parse] (ISO), then [DateTimeComponents.Formats.RFC_1123], then
 * [LocalDateTime.parse] (ISO local).
 *
 * This is necessary because the Pebble store API doesn't use the ISO 8601 format, which is what [Instant]s get
 * serialized as.
 */
object PebbleAPIInstantSerializer : KSerializer<Instant> {
   override val descriptor: SerialDescriptor
      get() = PrimitiveSerialDescriptor("com.matejdro.micropebble.appstore.api.serializer.Instant", PrimitiveKind.STRING)

   override fun serialize(encoder: Encoder, value: Instant) =
      encoder.encodeString(value.toString())

   override fun deserialize(decoder: Decoder) = tryAllOfThem(decoder.decodeString())
}
