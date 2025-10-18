package io.rebble.cobble.speexcodec

import java.nio.ByteBuffer

class SpeexCodec(
   private val sampleRate: Long,
   private val bitRate: Int,
   private val frameSize: Int,
   private val preprocessors: Set<Preprocessor> = emptySet(),
   @Suppress("UnusedPrivateProperty") // Used in native code
   private val gain: Float = 1.5f,
) : AutoCloseable {
   @Suppress("MagicNumber") // Evident
   enum class Preprocessor(val flagValue: Int) {
      DENOISE(1),
      AGC(2),
      VAD(4),
   }

   init {
      initNative()
   }

   private val speexDecBits: Long = initSpeexBits()
   private val speexDecState: Long = initDecState(sampleRate, bitRate)
   private val speexPreprocessState: Long = initPreprocessState(
      preprocessors.fold(0) { acc, preprocessor -> acc or preprocessor.flagValue },
      sampleRate.toInt(),
      frameSize
   )

   /**
    * Decode a frame of audio data.
    */
   fun decodeFrame(encodedFrame: ByteArray, decodedFrame: ByteBuffer, hasHeaderByte: Boolean = true): SpeexDecodeResult {
      return SpeexDecodeResult.Companion.fromInt(decode(encodedFrame, decodedFrame, hasHeaderByte))
   }

   override fun close() {
      destroySpeexBits(speexDecBits)
      destroyDecState(speexDecState)
      destroyPreprocessState(speexPreprocessState)
   }

   private external fun initNative()
   private external fun decode(encodedFrame: ByteArray, decodedFrame: ByteBuffer, hasHeaderByte: Boolean): Int
   private external fun initSpeexBits(): Long
   private external fun initDecState(sampleRate: Long, bitRate: Int): Long
   private external fun initPreprocessState(preprocessors: Int, sampleRate: Int, frameSize: Int): Long
   private external fun destroyPreprocessState(preprocessState: Long)
   private external fun destroySpeexBits(speexBits: Long)
   private external fun destroyDecState(decState: Long)

   companion object {
      // Used to load the 'speex_codec' library on application startup.
      init {
         System.loadLibrary("speex_codec")
      }
   }
}
