package com.matejdro.micropebble.bluetooth

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.rebble.libpebblecommon.LibPebbleConfig
import io.rebble.libpebblecommon.connection.AppContext
import io.rebble.libpebblecommon.connection.FirmwareUpdateCheckResult
import io.rebble.libpebblecommon.connection.LibPebble
import io.rebble.libpebblecommon.connection.LibPebble3
import io.rebble.libpebblecommon.connection.TokenProvider
import io.rebble.libpebblecommon.connection.WebServices
import io.rebble.libpebblecommon.services.WatchInfo
import io.rebble.libpebblecommon.voice.TranscriptionProvider
import io.rebble.libpebblecommon.voice.TranscriptionResult
import io.rebble.libpebblecommon.voice.VoiceEncoderInfo
import io.rebble.libpebblecommon.web.LockerModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.uuid.Uuid

@ContributesTo(AppScope::class)
interface LibPebbleFactory {
   @Provides
   @SingleIn(AppScope::class)
   fun createLibPebble(
      context: Context,
   ): LibPebble {
      val dummyWebServices = object : WebServices {
         override suspend fun fetchLocker(): LockerModel? {
            return null
         }

         override suspend fun removeFromLocker(id: Uuid): Boolean {
            return true
         }

         override suspend fun checkForFirmwareUpdate(watch: WatchInfo): FirmwareUpdateCheckResult? {
            return null
         }

         override suspend fun uploadMemfaultChunk(
            chunk: ByteArray,
            watchInfo: WatchInfo,
         ) {
         }
      }

      val dummyTokenProvider = object : TokenProvider {
         override suspend fun getDevToken(): String? {
            return null
         }
      }

      val dummyTranscriptionProvider = object : TranscriptionProvider {
         override suspend fun transcribe(
            encoderInfo: VoiceEncoderInfo,
            audioFrames: Flow<UByteArray>,
         ): TranscriptionResult {
            return TranscriptionResult.Disabled
         }

         override suspend fun canServeSession(): Boolean {
            return false
         }
      }

      return LibPebble3.create(
         LibPebbleConfig(),
         dummyWebServices,
         AppContext(context),
         dummyTokenProvider,
         MutableStateFlow(null),
         dummyTranscriptionProvider
      ).also { it.init() }
   }
}
