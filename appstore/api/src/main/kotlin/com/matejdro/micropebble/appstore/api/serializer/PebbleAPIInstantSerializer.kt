package com.matejdro.micropebble.appstore.api.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

/**
 * The first one that doesn't throw goes
 */
private val parsers: List<(String) -> Instant> = listOf(
   {
      LocalDateTime.parse(it, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant(ZoneOffset.UTC).toKotlinInstant()
   },
   {
      LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toInstant(ZoneOffset.UTC).toKotlinInstant()
   },
   {
      LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME).toInstant(ZoneOffset.UTC).toKotlinInstant()
   },
)

private fun tryAllOfThem(input: String): Instant {
   for (parser in parsers) {
      return runCatching { parser(input) }.getOrNull() ?: continue
   }
   error("All parsers failed \uD83D\uDE2D\uD83D\uDE2D")
}

/**
 * Serializes [java.time.OffsetDateTime] dates with [DateTimeFormatter.RFC_1123_DATE_TIME] first, then
 * [DateTimeFormatter.ISO_DATE_TIME].
 *
 * This is necessary because the Pebble store API doesn't use the ISO 8601 format, which is what [Instant]s get
 * serialized as.
 */
object PebbleAPIInstantSerializer : KSerializer<Instant> {
   override val descriptor: SerialDescriptor
      get() = PrimitiveSerialDescriptor("com.matejdro.micropebble.appstore.api.serializer.Instant", PrimitiveKind.STRING)

   override fun serialize(encoder: Encoder, value: Instant) =
      encoder.encodeString(value.toJavaInstant().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME))

   override fun deserialize(decoder: Decoder) = tryAllOfThem(decoder.decodeString())
}
