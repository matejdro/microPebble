package com.matejdro.micropebble.bluetooth

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.rebble.libpebblecommon.LibPebbleConfig
import io.rebble.libpebblecommon.WatchConfig
import io.rebble.libpebblecommon.connection.AppContext
import io.rebble.libpebblecommon.connection.FirmwareUpdateCheckResult
import io.rebble.libpebblecommon.connection.LibPebble
import io.rebble.libpebblecommon.connection.LibPebble3
import io.rebble.libpebblecommon.connection.TokenProvider
import io.rebble.libpebblecommon.connection.WebServices
import io.rebble.libpebblecommon.services.WatchInfo
import io.rebble.libpebblecommon.voice.TranscriptionProvider
import io.rebble.libpebblecommon.web.LockerModelWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.uuid.Uuid

@ContributesTo(AppScope::class)
interface LibPebbleFactory {
   @Provides
   @SingleIn(AppScope::class)
   fun createLibPebble(
      context: Context,
      transcriptionProvider: TranscriptionProvider,
   ): LibPebble {
      val dummyWebServices = object : WebServices {
         override suspend fun fetchLocker(): LockerModelWrapper? {
            return null
         }

         override suspend fun removeFromLocker(id: Uuid): Boolean {
            return true
         }

         override suspend fun checkForFirmwareUpdate(watch: WatchInfo): FirmwareUpdateCheckResult {
            return FirmwareUpdateCheckResult.FoundNoUpdate
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

      return LibPebble3.create(
         LibPebbleConfig(
            watchConfig = WatchConfig(lanDevConnection = true)
         ),
         dummyWebServices,
         AppContext(context),
         dummyTokenProvider,
         MutableStateFlow(null),
         transcriptionProvider
      ).also { it.init() }
   }
}
