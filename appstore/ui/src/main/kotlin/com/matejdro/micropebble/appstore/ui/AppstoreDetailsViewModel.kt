package com.matejdro.micropebble.appstore.ui

import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.collection.AppstoreCollectionPage
import com.matejdro.micropebble.appstore.ui.common.getHttpClient
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.common.util.joinUrls
import com.matejdro.micropebble.navigation.keys.AppstoreDetailsScreenKey
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import io.ktor.client.call.body
import io.ktor.client.request.get
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

   private val _appDataState = MutableStateFlow<Outcome<Application>>(Outcome.Progress())
   val appDataState: StateFlow<Outcome<Application>> = _appDataState

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

      val source = key.appstoreSource
      if (key.onlyPartialData && source != null) {
         resources.launchResourceControlTask(_appDataState) {
            val realData =
               getHttpClient()
                  .get(source.url.joinUrls("/v1/apps/id/${key.app.id}"))
                  .body<AppstoreCollectionPage>().apps.first()
            emit(Outcome.Success(realData))
         }
      } else {
         _appDataState.value = Outcome.Success(key.app)
      }
   }

   fun install() = resources.launchResourceControlTask(_appState) {
      val app = appDataState.value
      if (app !is Outcome.Success) {
         return@launchResourceControlTask
      }
      actionLogger.logAction { "AppstoreDetailsViewModel.install()" }
      val uri = URL(app.data.latestRelease.pbwFile)
      actionLogger.logAction { "WatchappListViewModel.install()" }

      withDefault {
         val tmpFile = File.createTempFile(app.data.uuid.toString(), "pbw")

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
