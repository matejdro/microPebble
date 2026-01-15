package com.matejdro.micropebble.appstore.ui

import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.AppstoreDetailsScreenKey
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import io.rebble.libpebblecommon.connection.LockerApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.io.files.Path
import okio.buffer
import okio.sink
import okio.source
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.io.File
import java.io.IOException
import java.net.URL

enum class AppInstallState {
   CAN_INSTALL,
   INSTALLED,
}

class AppSideloadFailed : CauseException(message = "App failed to sideload", isProgrammersFault = false)
class AppDownloadFailed(cause: IOException) : CauseException(message = "App download failed", cause)

@Inject
@ContributesScopedService
class AppstoreDetailsViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val lockerApi: LockerApi,
) : SingleScreenViewModel<AppstoreDetailsScreenKey>(resources.scope) {
   private val _appState = MutableStateFlow<Outcome<AppInstallState>>(Outcome.Progress())
   val appState: StateFlow<Outcome<AppInstallState>> = _appState

   override fun onServiceRegistered() {
      actionLogger.logAction { "AppstoreDetailsViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_appState) {
         emit(
            if (lockerApi.getLockerApp(key.app.uuid).first() != null) {
               Outcome.Success(AppInstallState.INSTALLED)
            } else {
               Outcome.Success(AppInstallState.CAN_INSTALL)
            }
         )
      }
   }

   fun install() = resources.launchResourceControlTask(_appState) {
      actionLogger.logAction { "AppstoreDetailsViewModel.install()" }
      val uri = URL(key.app.latestRelease.pbwFile)
      actionLogger.logAction { "WatchappListViewModel.install()" }

      withDefault {
         val tmpFile = File.createTempFile(key.app.uuid.toString(), "pbw")

         try {
            uri.openConnection().getInputStream().use { input ->
               tmpFile.sink().buffer().use {
                  it.writeAll(input.source())
               }
            }
         } catch (e: IOException) {
            emit(Outcome.Error(AppDownloadFailed(e)))
         }

         try {
            val result = lockerApi.sideloadApp(Path(tmpFile.absolutePath))
            if (result) {
               emit(Outcome.Success(AppInstallState.INSTALLED))
            } else {
               emit(Outcome.Error(AppSideloadFailed()))
            }
         } finally {
            tmpFile.delete()
         }
      }
   }
}
