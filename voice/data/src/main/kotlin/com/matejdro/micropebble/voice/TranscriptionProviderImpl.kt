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
import kotlinx.coroutines.selects.select
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

   // Decode and pipe watch audio to the SpeechRecognizer, breaking three possible
   // deadlocks:
   //   1. Recognizer fires onResults early and stops reading the pipe: the pipe
   //      fills and the next write() blocks forever.
   //   2. Recognizer never fires onResults (waiting for EOF) while audio source
   //      ends naturally via StopTransfer: same pipe-fills-then-blocks symptom,
   //      but we never see the recognizer signal completion.
   //   3. Recognizer is slow consuming the pipe while the watch streams faster
   //      than it can process: pipe fills mid-stream, write blocks before either
   //      audio end or recognition complete signals fire.
   //
   // Decode runs on its own coroutine and feeds an unbounded channel — it always
   // drains the source flow regardless of how fast the writer can keep up.
   // A watcher coroutine closes the pipe (interrupting any blocked write with
   // IOException) when either the recognizer completes or the audio source ends.
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
      val audioEnded = CompletableDeferred<Unit>()

      val closeWatcher = launch {
         select<Unit> {
            finishedReceiver.onAwait { }
            audioEnded.onAwait { }
         }
         logcat { "Closing audio pipe to deliver EOF to recognizer" }
         runCatching { writeStream.close() }
      }

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
            audioEnded.complete(Unit)
         }
      }

      try {
         for (decoded in decodedFrames) {
            this@TranscriptionProviderImpl.logcat { "Wrote audio stream packet" }
            if (finishedReceiver.isCompleted) continue
            writeStream.write(decoded, 0, targetBufferSize)
         }
      } catch (e: IOException) {
         logcat { "Pipe closed during write: ${e.message ?: "no message"}" }
      } finally {
         decoder.cancel()
         closeWatcher.cancel()
      }
   }

   override suspend fun canServeSession(): Boolean {
      return true
   }
}

// Settings.Secure.VOICE_RECOGNITION_SERVICE is @hide; the underlying key is stable.
private const val VOICE_RECOGNITION_SERVICE_SETTING = "voice_recognition_service"
