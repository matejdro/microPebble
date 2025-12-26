package com.materjdro.micropebble.voice

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Build
import android.os.ParcelFileDescriptor
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi
import coredevices.speex.SpeexCodec
import coredevices.speex.SpeexDecodeResult
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.voice.TranscriptionProvider
import io.rebble.libpebblecommon.voice.TranscriptionResult
import io.rebble.libpebblecommon.voice.VoiceEncoderInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import si.inova.kotlinova.core.logging.logcat
import si.inova.kotlinova.core.reporting.ErrorReporter
import kotlin.coroutines.cancellation.CancellationException

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
      logcat { "Start transcribe" }
      // SpeechRecognizer's audio source is only supported on 13+
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
         logcat { "Android version fail" }
         return TranscriptionResult.Error("Voice Requires Android 13 or above")
      }

      try {
         val speexInfo = (encoderInfo as? VoiceEncoderInfo.Speex) ?: error("Unsupported codec: $encoderInfo")
         SpeexCodec(
            speexInfo.sampleRate,
            speexInfo.bitRate,
            speexInfo.frameSize,
         ).use { speexDecoder ->
            val (readPipe, writePipe) = ParcelFileDescriptor.createPipe()

            readPipe.use {
               ParcelFileDescriptor.AutoCloseOutputStream(writePipe).use { writeStream ->
                  val finishedReceiver = CompletableDeferred<TranscriptionResult>()
                  this@TranscriptionProviderImpl.logcat { "Attempt to create speech recognizer" }
                  val speechRecognizer = createSpeechRecognizer(readPipe, speexInfo, finishedReceiver)
                  this@TranscriptionProviderImpl.logcat { "Created speech recognizer" }

                  return try {
                     decodeWatchAudioIntoSpeechRecognizerStream(
                        speexInfo,
                        audioFrames,
                        speexDecoder,
                        writeStream,
                        finishedReceiver
                     )
                     this@TranscriptionProviderImpl.logcat { "Submitted all the audio, awaiting completed recognition" }
                     finishedReceiver.await()
                        .also {
                           this@TranscriptionProviderImpl.logcat { "Recognition completed" }
                        }
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

   @RequiresApi(Build.VERSION_CODES.TIRAMISU)
   private suspend fun createSpeechRecognizer(
      readPipe: ParcelFileDescriptor,
      speexInfo: VoiceEncoderInfo.Speex,
      finishedReceiver: CompletableDeferred<TranscriptionResult>,
   ): SpeechRecognizer = withContext(Dispatchers.Main) {
      val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, RecognizerIntent.EXTRA_MAX_RESULTS)
      intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, readPipe)
      intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
      intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
      intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, speexInfo.sampleRate)

      speechRecognizer.setRecognitionListener(RecognitionListenerImpl(finishedReceiver))
      speechRecognizer.startListening(intent)
      speechRecognizer
   }

   @Suppress("BlockingMethodInNonBlockingContext") // We already are on IO
   private suspend fun decodeWatchAudioIntoSpeechRecognizerStream(
      speexInfo: VoiceEncoderInfo.Speex,
      audioFrames: Flow<UByteArray>,
      speexDecoder: SpeexCodec,
      writeStream: ParcelFileDescriptor.AutoCloseOutputStream,
      finishedReceiver: CompletableDeferred<TranscriptionResult>,
   ) {
      val targetBufferSize = Short.SIZE_BYTES * speexInfo.frameSize
      val targetBuffer = ByteArray(targetBufferSize)

      audioFrames.collect {
         val result = speexDecoder.decodeFrame(it.asByteArray(), targetBuffer, hasHeaderByte = true)
         if (result != SpeexDecodeResult.Success) {
            error("Speex decoding failed: $result")
         }
         this@TranscriptionProviderImpl.logcat { "Wrote audio stream packet" }

         if (finishedReceiver.isCompleted) {
            // Speech recognizer has already finished. We cannot write more data to it
         } else {
            writeStream.write(targetBuffer, 0, targetBufferSize)
         }
      }
      writeStream.close()
   }

   override suspend fun canServeSession(): Boolean {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && SpeechRecognizer.isRecognitionAvailable(context)
   }
}
