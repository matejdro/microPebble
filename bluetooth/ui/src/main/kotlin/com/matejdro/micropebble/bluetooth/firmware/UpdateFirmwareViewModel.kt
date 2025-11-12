package com.matejdro.micropebble.bluetooth.firmware

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Stable
import com.matejdro.micropebble.bluetooth.errors.InvalidPbzFileException
import com.matejdro.micropebble.common.exceptions.LibPebbleError
import com.matejdro.micropebble.common.exceptions.WatchDisconnectedException
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.FirmwareUpdateScreenKey
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.Watches
import io.rebble.libpebblecommon.connection.endpointmanager.FirmwareUpdateException
import io.rebble.libpebblecommon.connection.endpointmanager.FirmwareUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import okio.buffer
import okio.sink
import okio.source
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.io.File

@Stable
@Inject
@ContributesScopedService
class UpdateFirmwareViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val watches: Watches,
   private val context: Context,
) : SingleScreenViewModel<FirmwareUpdateScreenKey>(resources.scope) {
   private val _watchInfo = MutableStateFlow<Outcome<UpdateFirmwareState>>(Outcome.Progress())
   val watchInfo: StateFlow<Outcome<UpdateFirmwareState>> = _watchInfo

   private val _updateStatus = MutableStateFlow<Outcome<Unit?>>(Outcome.Success(null))
   val updateStatus: StateFlow<Outcome<Unit?>> = _updateStatus

   override fun onServiceRegistered() {
      actionLogger.logAction { "UpdateFirmwareViewModel.onServiceRegistered()" }
      resources.launchResourceControlTask(_watchInfo) {
         val watch = watches.watches.first()
            .filterIsInstance<ConnectedPebbleDevice>()
            .filter { key.watchSerial == null || key.watchSerial == it.serial }
            .firstOrNull()
            ?: throw WatchDisconnectedException()

         emit(Outcome.Success(UpdateFirmwareState(watch, key.pbzUri)))
      }
   }

   fun selectPbz(pbzUri: Uri) {
      actionLogger.logAction { "UpdateFirmwareViewModel.selectPbz(pbzUri = $pbzUri)" }
      _watchInfo.update { outcome -> outcome.mapData { it.copy(pendingFirmwareUrl = pbzUri) } }
   }

   fun startInstall() = resources.launchResourceControlTask(_updateStatus) {
      actionLogger.logAction { "UpdateFirmwareViewModel.startInstall()" }

      val watchSerial = watchInfo.value.data?.watch?.serial ?: throw IllegalArgumentException("Got null watch")
      val uri = watchInfo.value.data?.pendingFirmwareUrl ?: throw IllegalArgumentException("Got null firmware")

      // Reset the PBZ so user can re-select a different one
      _watchInfo.update { outcome -> outcome.mapData { it.copy(pendingFirmwareUrl = null) } }

      val statusFlow = watches.watches.map { allWatches ->
         allWatches.filterIsInstance<ConnectedPebbleDevice>().firstOrNull { it.serial == watchSerial }?.firmwareUpdateState
      }

      withDefault {
         if (getFileName(uri)?.endsWith(".pbz") != true) {
            throw InvalidPbzFileException()
         }

         val tmpFile = copyFirmwareToTempFile(uri)
         val statusChannel = statusFlow.buffer(Channel.BUFFERED).produceIn(this)

         try {
            val watch = watches.watches.first().filterIsInstance<ConnectedPebbleDevice>().firstOrNull { it.serial == watchSerial }
               ?: throw WatchDisconnectedException()
            watch.sideloadFirmware(Path(tmpFile.absolutePath))

            observeFirmwareUpdateStatus(statusChannel, this@withDefault, this@launchResourceControlTask)

            emit(Outcome.Success(Unit))
         } finally {
            tmpFile.delete()
            Unit // Extra Unit to not trip up detekt
         }
      }
   }

   private suspend fun observeFirmwareUpdateStatus(
      statusChannel: ReceiveChannel<FirmwareUpdater.FirmwareUpdateStatus?>,
      scope: CoroutineScope,
      block: CoroutineResourceManager.ResourceControlBlock<Unit?>,
   ) {
      var stopOnIdle = false
      var progressJob: Job? = null

      for (status in statusChannel) {
         when (status) {
            is FirmwareUpdater.FirmwareUpdateStatus.InProgress -> {
               stopOnIdle = true
               progressJob?.cancel()
               progressJob = scope.launch {
                  status.progress.collect {
                     block.emit(Outcome.Progress(progress = it))
                  }
               }
            }

            is FirmwareUpdater.FirmwareUpdateStatus.WaitingForReboot -> {
               stopOnIdle = true
               progressJob?.cancel()
               block.emit(Outcome.Progress(progress = null))
            }

            is FirmwareUpdater.FirmwareUpdateStatus.WaitingToStart,
            null,
            -> {
               stopOnIdle = true
            }

            is FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.ErrorStarting -> {
               throw LibPebbleError(status.error.name)
            }

            is FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle -> {
               val lastFailure = status.lastFailure
               if (lastFailure != null) {
                  throw if (lastFailure is FirmwareUpdateException) {
                     LibPebbleError(lastFailure.message, lastFailure)
                  } else {
                     lastFailure
                  }
               } else if (stopOnIdle) {
                  break
               }
            }
         }
      }
   }

   private fun copyFirmwareToTempFile(uri: Uri): File {
      val tmpFile = File.createTempFile("pbz", null)
      val stream =
         requireNotNull(context.contentResolver.openInputStream(uri)) {
            "Files provider should not return null streams"
         }

      tmpFile.sink().use { fileSink ->
         stream.source().buffer().use { it.readAll(fileSink) }
      }
      return tmpFile
   }

   private fun getFileName(uri: Uri): String? {
      val projection = arrayOf<String?>(MediaStore.MediaColumns.DISPLAY_NAME)
      return context.contentResolver.query(uri, projection, null, null, null).use {
         if (it?.moveToFirst() != true) return@use null
         it.getString(0)
      }
   }
}

data class UpdateFirmwareState(
   val watch: ConnectedPebbleDevice,
   val pendingFirmwareUrl: Uri? = null,
)
