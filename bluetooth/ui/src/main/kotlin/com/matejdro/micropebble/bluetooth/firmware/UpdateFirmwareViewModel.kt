package com.matejdro.micropebble.bluetooth.firmware

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.matejdro.micropebble.bluetooth.errors.InvalidPbzFileException
import com.matejdro.micropebble.common.exceptions.LibPebbleError
import com.matejdro.micropebble.common.exceptions.WatchDisconnectedException
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.FirmwareUpdateScreenKey
import com.matejdro.micropebble.navigation.keys.common.InputFile
import com.matejdro.micropebble.webservices.api.GithubAsset
import com.matejdro.micropebble.webservices.api.GithubRelease
import com.matejdro.micropebble.webservices.api.GithubSource
import com.matejdro.micropebble.webservices.api.WebservicesClient
import dev.zacsweers.metro.Inject
import dispatch.core.dispatcherProvider
import dispatch.core.withDefault
import io.rebble.libpebblecommon.connection.CommonConnectedDevice
import io.rebble.libpebblecommon.connection.Watches
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.connection.endpointmanager.FirmwareUpdateException
import io.rebble.libpebblecommon.services.FirmwareVersion
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
import logcat.logcat
import okio.buffer
import okio.sink
import okio.source
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.io.File
import kotlin.time.Instant
import kotlin.time.Instant.Companion.DISTANT_PAST

@Stable
@Inject
@ContributesScopedService
class UpdateFirmwareViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val watches: Watches,
   private val context: Context,
   private val webservicesClient: WebservicesClient,
) : SingleScreenViewModel<FirmwareUpdateScreenKey>(resources.scope) {
   private val _watchInfo = MutableStateFlow<Outcome<UpdateFirmwareState>>(Outcome.Progress())
   val watchInfo: StateFlow<Outcome<UpdateFirmwareState>> = _watchInfo

   private val _updateStatus = MutableStateFlow<Outcome<Unit?>>(Outcome.Success(null))
   val updateStatus: StateFlow<Outcome<Unit?>> = _updateStatus
   
   // State for GitHub releases
   private val _githubReleases = MutableStateFlow<Outcome<List<GithubRelease>?>>(Outcome.Success(null))
   val githubReleases: StateFlow<Outcome<List<GithubRelease>?>> = _githubReleases
   
   // State for download progress
   private val _downloadProgress = MutableStateFlow<Outcome<File>>(Outcome.Success(File("")))
   val downloadProgress: StateFlow<Outcome<File>> = _downloadProgress
   
   // Whether GitHub auto-download is available for this watch
   val isGithubAutoDownloadAvailable: Boolean
      get() {
         val watch = watchInfo.value.data?.watch
         val revision = watch?.watchType?.revision?.lowercase() ?: return false
         return revision.startsWith("asterix") || 
                revision.startsWith("obelix") || 
                revision.startsWith("getafix")
      }

   override fun onServiceRegistered() {
      actionLogger.logAction { "UpdateFirmwareViewModel.onServiceRegistered()" }
      resources.launchResourceControlTask(_watchInfo) {
         val watch = watches.watches.first()
            .filterIsInstance<CommonConnectedDevice>()
            .firstOrNull { key.watchSerial == null || key.watchSerial == it.serial }
            ?: throw WatchDisconnectedException()

         emit(Outcome.Success(UpdateFirmwareState(watch, key.pbzFile)))
      }
   }

   fun selectPbz(pbzUri: Uri) =
      resources.launchWithExceptionReporting(coroutineScope.coroutineContext.dispatcherProvider.default) {
         actionLogger.logAction { "UpdateFirmwareViewModel.selectPbz(pbzUri = $pbzUri)" }
         _watchInfo.update { outcome ->
            outcome.mapData { it.copy(pendingFirmware = InputFile(pbzUri, getFileName(pbzUri).orEmpty())) }
         }
      }

   /**
    * Check for firmware updates on GitHub from the default sources.
    */
   fun checkGithubUpdates(source: GithubSource = GithubSource.defaultSources[0]) =
      resources.launchResourceControlTask(_githubReleases) {
         actionLogger.logAction { "UpdateFirmwareViewModel.checkGithubUpdates(source = ${source.fullName})" }
         
         // Clear previous releases
         emit(Outcome.Progress())
         
         logcat { "Fetching releases from ${source.fullName} (URL: ${source.apiUrl}/releases)" }
         
         val result = webservicesClient.fetchGithubReleases(source, null)
         
         if (result is Outcome.Success) {
            val releases = result.data
            
            // Get the current watch to filter by hardware platform and version
            val currentWatch = watchInfo.value.data?.watch
            val watchRevision = currentWatch?.watchType?.revision?.lowercase()
            val currentFwVersion = currentWatch?.watchInfo?.runningFwVersion
            logcat { "Current watch revision: $watchRevision, firmware version: ${currentFwVersion?.stringVersion}" }
            
            // Filter releases to only include PBZ assets matching the watch hardware platform
            val filteredReleases = releases.map { release ->
               // Filter PBZ assets by watch revision if we have one
               val filteredAssets = if (watchRevision != null) {
                  release.assets.filter { asset ->
                     asset.name.endsWith(".pbz", ignoreCase = true) &&
                     asset.name.contains(watchRevision, ignoreCase = true)
                  }
               } else {
                  release.assets.filter { asset ->
                     asset.name.endsWith(".pbz", ignoreCase = true)
                  }
               }
               
               // Create a new release with only the filtered PBZ assets
               release.copy(assets = filteredAssets)
            }.filter { release ->
               // Only keep releases that have at least one PBZ asset
               release.assets.isNotEmpty()
            }
            
            // Filter out releases older than current firmware version
            val newerReleases = if (currentFwVersion != null) {
               filteredReleases.filter { release ->
                  // Try to parse the version from tag_name
                  val releaseVersion = FirmwareVersion.from(
                     tag = release.tag_name,
                     isRecovery = false,
                     gitHash = "",
                     timestamp = DISTANT_PAST,
                     isDualSlot = false,
                     isSlot0 = false
                  )
                  // If we can't parse the version, keep the release (be safe)
                  releaseVersion?.let { it > currentFwVersion } ?: true
               }
            } else {
               filteredReleases
            }
            
            logcat { "Successfully found ${newerReleases.size} releases from ${source.fullName} with matching PBZ assets" }
            if (newerReleases.isNotEmpty()) {
               newerReleases.forEach { release ->
                  logcat { "  Release: ${release.tag_name} (${release.pbzAssets.size} PBZ assets)" }
               }
            } else if (currentFwVersion != null) {
               logcat { "Watch is up to date (current version: ${currentFwVersion.stringVersion})" }
            }
            
            // Emit empty list if no newer releases found (watch is up to date)
            // or the filtered list if there are updates available
            emit(Outcome.Success(newerReleases))
         } else if (result is Outcome.Error) {
            logcat { "Error fetching releases: ${result.exception.message}" }
            result.exception.cause?.let { logcat { "Cause: ${it.message}" } }
            emit(Outcome.Error(result.exception))
         }
      }

   /**
    * Download a firmware file from GitHub and prepare it for installation.
    */
   fun downloadFromGithub(asset: GithubAsset) =
      resources.launchResourceControlTask(_downloadProgress) {
         actionLogger.logAction { "UpdateFirmwareViewModel.downloadFromGithub(asset = ${asset.name})" }
         
         emit(Outcome.Progress())
         
         val result = webservicesClient.downloadGithubAsset(asset, null)
         
         if (result is Outcome.Success) {
            val downloadedFile = result.data
            logcat { "Downloaded firmware file: ${downloadedFile.absolutePath}" }
            
            // Convert the file to an InputFile so it can be installed
            val inputFile = InputFile(
               uri = Uri.fromFile(downloadedFile),
               filename = asset.name
            )
            
            // Set it as the pending firmware
            _watchInfo.update { outcome ->
               outcome.mapData { it.copy(pendingFirmware = inputFile) }
            }
            
            // Reset download progress so user can select another file
            emit(Outcome.Success(File("")))
         } else if (result is Outcome.Error) {
            logcat { "Error downloading asset: ${result.exception}" }
            emit(Outcome.Error(result.exception))
         }
      }

   fun startInstall() = resources.launchResourceControlTask(_updateStatus) {
      actionLogger.logAction { "UpdateFirmwareViewModel.startInstall()" }

      val watchSerial = watchInfo.value.data?.watch?.serial ?: throw IllegalArgumentException("Got null watch")
      val file = watchInfo.value.data?.pendingFirmware ?: throw IllegalArgumentException("Got null firmware")

      // Reset the PBZ so user can re-select a different one
      _watchInfo.update { outcome -> outcome.mapData { it.copy(pendingFirmware = null) } }

      val statusFlow = watches.watches.map { allWatches ->
         allWatches.filterIsInstance<CommonConnectedDevice>().firstOrNull { it.serial == watchSerial }?.firmwareUpdateState
      }

      withDefault {
         if (file.filename.endsWith(".pbz") != true) {
            throw InvalidPbzFileException()
         }

         val tmpFile = copyFirmwareToTempFile(file.uri)
         val statusChannel = statusFlow.buffer(Channel.BUFFERED).produceIn(this)

         try {
            val watch =
               watches.watches.first().filterIsInstance<CommonConnectedDevice>().firstOrNull { it.serial == watchSerial }
                  ?: throw WatchDisconnectedException()
            watch.sideloadFirmware(Path(tmpFile.absolutePath))

            observeFirmwareUpdateStatus(
               statusChannel = statusChannel,
               scope = this@withDefault,
               block = this@launchResourceControlTask,
            )

            emit(Outcome.Success(Unit))
         } finally {
            tmpFile.delete()
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
                  status.progress.collect { progress ->
                     block.emit(Outcome.Progress(progress = progress))
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
      val tmpFile = File(context.cacheDir, "firmware.pbz")
      logcat { "Copying firmware to the ${tmpFile.absolutePath}" }

      context.contentResolver.openInputStream(uri)?.use { stream ->
         tmpFile.sink().use { fileSink ->
            stream.source().buffer().use { it.readAll(fileSink) }
         }
      } ?: error("Files provider should not return null streams")

      logcat { "Firmware copied. Target size: ${tmpFile.length()} bytes" }
      return tmpFile
   }

   private fun getFileName(uri: Uri): String? {
      val projection = arrayOf<String?>(MediaStore.MediaColumns.DISPLAY_NAME)
      return context.contentResolver.query(uri, projection, null, null, null).use { cursor ->
         if (cursor?.moveToFirst() != true) return@use null
         cursor.getString(0)
      }
   }
}

@Immutable
data class UpdateFirmwareState(
   val watch: CommonConnectedDevice,
   val pendingFirmware: InputFile? = null,
   val githubReleases: List<GithubRelease>? = null,
)
