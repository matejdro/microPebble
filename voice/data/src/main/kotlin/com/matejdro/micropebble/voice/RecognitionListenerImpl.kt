package com.matejdro.micropebble.voice

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import com.matejdro.micropebble.voice.data.R
import io.rebble.libpebblecommon.voice.TranscriptionResult
import io.rebble.libpebblecommon.voice.TranscriptionWord
import kotlinx.coroutines.CompletableDeferred
import logcat.logcat
import si.inova.kotlinova.core.state.toMap

class RecognitionListenerImpl(
   private val context: Context,
   private val finishedReceiver: CompletableDeferred<TranscriptionResult>,
) : RecognitionListener {
   private var partialText: String? = null
   private var partialConfidence: Float? = null

   override fun onError(error: Int) {
      logcat { "Got onError $error" }

      finishedReceiver.complete(
         when (error) {
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
            SpeechRecognizer.ERROR_SERVER,
            SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
            -> {
               TranscriptionResult.ConnectionError("Network error")
            }

            SpeechRecognizer.ERROR_NO_MATCH -> TranscriptionResult.Failed

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
               TranscriptionResult.Success(
                  context.getString(R.string.error_voice_not_enabled_short)
                     .split(" ").map { TranscriptionWord(it, 1f) }
               )

            else -> TranscriptionResult.Error("Unknown error: $error")
         }
      )
   }

   override fun onResults(results: Bundle) {
      logcat { "Got results ${results.toMap()}" }
      val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
         ?: partialText
      val confidence = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.firstOrNull()
         ?: partialConfidence
         ?: DEFAULT_CONFIDENCE

      finishedReceiver.complete(
         if (text == null) {
            TranscriptionResult.Failed
         } else {
            TranscriptionResult.Success(
               text.split(" ").map {
                  TranscriptionWord(it, confidence)
               }
            )
         }
      )
   }

   override fun onPartialResults(partialResults: Bundle) {
      logcat { "Got partial results" }

      partialText = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
      partialConfidence = partialResults.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.firstOrNull()
   }

   override fun onEvent(eventType: Int, params: Bundle?) {}
   override fun onBeginningOfSpeech() {}
   override fun onBufferReceived(buffer: ByteArray?) {}
   override fun onEndOfSpeech() {}
   override fun onReadyForSpeech(params: Bundle?) {}
   override fun onRmsChanged(rmsdB: Float) {}
}

private const val DEFAULT_CONFIDENCE = 0.5f
