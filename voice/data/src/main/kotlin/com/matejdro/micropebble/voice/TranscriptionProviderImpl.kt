package com.matejdro.micropebble.voice

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi
import com.matejdro.micropebble.voice.data.R
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.logcat
import si.inova.kotlinova.core.reporting.ErrorReporter
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

@Inject
@ContributesBinding(AppScope::class)
class TranscriptionProviderImpl(
   private val context: Context,
   private val errorReporter: ErrorReporter,
   private val voiceSetupNotifier: VoiceSetupNotifier,
) : TranscriptionProvider {
   @Suppress("NestedBlockDepth") // It uses a lot of .use {}, which increase nesting. It's fine otherwise.
   override suspend fun transcribe(
      encoderInfo: VoiceEncoderInfo,
      audioFrames: Flow<UByteArray>,
      isNotificationReply: Boolean,
   ): TranscriptionResult {
      logcat { "Start transcribe" }
      // SpeechRecognizer's audio source is only supported on 13+
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
         logcat { "Android version fail" }
         return staticSuccess(context.getString(R.string.error_voice_android_version))
      }

      if (!SpeechRecognizer.isRecognitionAvailable(context)) {
         return staticSuccess(context.getString(R.string.error_no_voice_recognizer))
      }

      // isRecognitionAvailable() returns true even when no service is the system default.
      val defaultService = Settings.Secure.getString(context.contentResolver, VOICE_RECOGNITION_SERVICE_SETTING)
      if (defaultService.isNullOrBlank()) {
         logcat { "No default voice recognition service selected" }
         voiceSetupNotifier.showNoDefaultVoiceServiceNotification()
         return staticSuccess(context.getString(R.string.error_no_default_voice_service))
      }

      try {
         val speexInfo = (encoderInfo as? VoiceEncoderInfo.Speex) ?: error("Unsupported codec: $encoderInfo")
         SpeexCodec(
            sampleRate = speexInfo.sampleRate,
            bitRate = speexInfo.bitRate,
            frameSize = speexInfo.frameSize,
         ).use { speexDecoder ->
            val (readPipe, writePipe) = ParcelFileDescriptor.createPipe()

            readPipe.use {
               ParcelFileDescriptor.AutoCloseOutputStream(writePipe).use { writeStream ->
                  val finishedReceiver = CompletableDeferred<TranscriptionResult>()
                  this@TranscriptionProviderImpl.logcat { "Attempt to create speech recognizer" }
                  val speechRecognizer = createSpeechRecognizer(readPipe, speexInfo, finishedReceiver)
                  this@TranscriptionProviderImpl.logcat { "Created speech recognizer" }

                  return try {
                     pipeAudioWithEarlyTermination(speexInfo, audioFrames, speexDecoder, writeStream, finishedReceiver)
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
      intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
      intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, readPipe)
      intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
      intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
      intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, speexInfo.sampleRate.toInt())

      speechRecognizer.setRecognitionListener(RecognitionListenerImpl(context, finishedReceiver))
      speechRecognizer.startListening(intent)
      speechRecognizer
   }

   // Decode and pipe watch audio to the SpeechRecognizer. Two coroutines plus
   // the main writer body handle the four termination paths:
   //
   //   1. Audio source ends (watch StopTransfer): decoder's finally closes
   //      the channel and the pipe — writer drains remaining items and exits;
   //      EOF on the pipe signals the recognizer to fire onResults.
   //   2. Recognizer fires onResults / onError early: finishedWaiter closes
   //      the pipe (interrupts any blocked write with IOException) and
   //      cancels the decoder; writer exits via closed channel or IOException.
   //   3. Recognizer is a slow consumer and the pipe fills mid-stream:
   //      the decoder keeps draining the source into the unbounded channel,
   //      so StopTransfer can still arrive — eventually path 1 or 2 fires.
   //   4. Recognizer fires early AND the watch sends no more audio and no
   //      StopTransfer: path 2's decoder.cancel() forces the decoder's
   //      finally, which closes the channel and frees the writer's for-loop.
   //
   // writeStream.close() is idempotent under runCatching, so both paths can
   // race to close the pipe without coordination.
   @Suppress("BlockingMethodInNonBlockingContext") // We already are on IO
   private suspend fun pipeAudioWithEarlyTermination(
      speexInfo: VoiceEncoderInfo.Speex,
      audioFrames: Flow<UByteArray>,
      speexDecoder: SpeexCodec,
      writeStream: ParcelFileDescriptor.AutoCloseOutputStream,
      finishedReceiver: CompletableDeferred<TranscriptionResult>,
   ): Unit = coroutineScope {
      val targetBufferSize = Short.SIZE_BYTES * speexInfo.frameSize
      val decodedFrames = Channel<ByteArray>(Channel.UNLIMITED)

      val decoder = launch {
         try {
            audioFrames.collect { frame ->
               val buffer = ByteArray(targetBufferSize)
               val result = speexDecoder.decodeFrame(
                  encodedFrame = frame.asByteArray(),
                  decodedFrame = buffer,
                  hasHeaderByte = true,
               )
               if (result != SpeexDecodeResult.Success) {
                  error("Speex decoding failed: $result")
               }
               decodedFrames.send(buffer)
            }
         } finally {
            decodedFrames.close()
            this@TranscriptionProviderImpl.logcat { "Audio source ended, closing pipe to deliver EOF" }
            runCatching { writeStream.close() }
         }
      }

      val finishedWaiter = launch {
         finishedReceiver.await()
         this@TranscriptionProviderImpl.logcat { "Recognizer finished, closing pipe and stopping decoder" }
         runCatching { writeStream.close() }
         decoder.cancel()
      }

      try {
         for (decoded in decodedFrames) {
            if (finishedReceiver.isCompleted) continue
            writeStream.write(decoded, 0, targetBufferSize)
         }
      } catch (e: IOException) {
         logcat { "Pipe closed during write: ${e.message ?: "no message"}" }
      } finally {
         decoder.cancel()
         finishedWaiter.cancel()
      }
   }

   override suspend fun canServeSession(): Boolean {
      return true
   }
}

// Settings.Secure.VOICE_RECOGNITION_SERVICE is @hide; the underlying key is stable.
private const val VOICE_RECOGNITION_SERVICE_SETTING = "voice_recognition_service"
