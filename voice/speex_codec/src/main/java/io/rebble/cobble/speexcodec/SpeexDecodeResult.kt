package io.rebble.cobble.speexcodec

enum class SpeexDecodeResult {
   Success,
   EndOfStream,
   CorruptStream,
   ;

   companion object {
      @Suppress("MagicNumber") // Evident
      fun fromInt(value: Int) = when (value) {
         0 -> Success
         -1 -> EndOfStream
         -2 -> CorruptStream
         else -> throw IllegalArgumentException("Invalid value for SpeexDecodeResult")
      }
   }
}
