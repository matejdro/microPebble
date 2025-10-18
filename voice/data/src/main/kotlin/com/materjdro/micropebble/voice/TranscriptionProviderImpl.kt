package com.materjdro.micropebble.voice

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.rebble.cobble.speex_codec.SpeexCodec
import io.rebble.cobble.speex_codec.SpeexDecodeResult
import io.rebble.libpebblecommon.voice.TranscriptionProvider
import io.rebble.libpebblecommon.voice.TranscriptionResult
import io.rebble.libpebblecommon.voice.TranscriptionWord
import io.rebble.libpebblecommon.voice.VoiceEncoderInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.core.state.toMap
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.sin

@Inject
@ContributesBinding(AppScope::class)
class TranscriptionProviderImpl(
   private val context: Context,
   private val errorReporter: ErrorReporter,
) : TranscriptionProvider {
   override suspend fun transcribe(
      encoderInfo: VoiceEncoderInfo,
      audioFrames: Flow<UByteArray>,
   ): TranscriptionResult {
      try {
         val speexInfo = (encoderInfo as? VoiceEncoderInfo.Speex) ?: error("Unsupported codec: $encoderInfo")
         SpeexCodec(
            speexInfo.sampleRate,
            speexInfo.bitRate,
            speexInfo.frameSize,
            setOf(SpeexCodec.Preprocessor.DENOISE, SpeexCodec.Preprocessor.AGC)
         ).use { speexDecoder ->
            val (readPipe, writePipe) = ParcelFileDescriptor.createPipe()

            readPipe.use {
               ParcelFileDescriptor.AutoCloseOutputStream(writePipe).use { writeStream ->
                  val finishedReceiver = CompletableDeferred<TranscriptionResult>()

                  val speechRecognizer = withContext(Dispatchers.Main) {
                     val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                     val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                     intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                     intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, RecognizerIntent.EXTRA_MAX_RESULTS)
                     intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, readPipe)
                     intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
                     intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
                     intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, speexInfo.sampleRate)

                     speechRecognizer.setRecognitionListener(object : RecognitionListener {
                        private var partialText: String? = null
                        private var partialConfidence: Float? = null

                        override fun onError(error: Int) {
                           finishedReceiver.complete(
                              when (error) {
                                 SpeechRecognizer.ERROR_NETWORK,
                                 SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                                 SpeechRecognizer.ERROR_SERVER,
                                 SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
                                    -> TranscriptionResult.ConnectionError("Network error")

                                 SpeechRecognizer.ERROR_NO_MATCH -> TranscriptionResult.Failed
                                 else -> TranscriptionResult.Error("Unknown error: $error")
                              }
                           )
                        }

                        override fun onResults(results: Bundle) {
                           val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                              ?: partialText
                           val confidence = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.firstOrNull()
                              ?: partialConfidence
                              ?: 0.5f

                           finishedReceiver.complete(
                              if (text == null) {
                                 TranscriptionResult.Failed
                              } else {
                                 TranscriptionResult.Success(text.split(" ").map {
                                    TranscriptionWord(it, confidence)
                                 }
                                 )
                              }
                           )
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                        override fun onBeginningOfSpeech() {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {}
                        override fun onPartialResults(partialResults: Bundle) {
                           partialText = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                           partialConfidence = partialResults.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.firstOrNull()
                        }

                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onRmsChanged(rmsdB: Float) {}
                     })
                     println("startListening")
                     speechRecognizer.startListening(intent)
                     speechRecognizer
                  }

                  return try {
                     val targetBufferSize = Short.SIZE_BYTES * speexInfo.frameSize
                     val targetBuffer = ByteBuffer.allocateDirect(targetBufferSize)
                     targetBuffer.order(ByteOrder.nativeOrder())

                     audioFrames.collect {
                        println("collect frame")
                        val result = speexDecoder.decodeFrame(it.asByteArray(), targetBuffer, hasHeaderByte = true)
                        if (result != SpeexDecodeResult.Success) {
                           error("Speex decoding failed: $result")
                        }

                        targetBuffer.rewind()

                        writeStream.write(targetBuffer.array(), 0, targetBufferSize)
                     }
                     writeStream.close()

                     println("waiting for end")
                     finishedReceiver.await().also { println("got $it") }
                  } finally {
                     speechRecognizer.destroy()
                  }
               }
            }

         }
      } catch (e: CancellationException) {
         throw e
      } catch (e: Exception) {
         errorReporter.report(Exception("Speech transcription failed", e))
         throw e
      }
   }

   override suspend fun canServeSession(): Boolean {
      return true
   }
}

fun generateSineWave(durationSeconds: Int, frequencyHz: Int): ShortArray {
   val totalSamples = 16000 * durationSeconds
   val samples = ShortArray(totalSamples)

   for (i in 0 until totalSamples) {
      val angle = 2.0 * Math.PI * i / (16000.0 / frequencyHz)
      // 16-bit PCM max amplitude is 32767
      samples[i] = (sin(angle) * Short.MAX_VALUE).toInt().toShort()
   }
   return samples
}
