package com.matejdro.micropebble.apps.ui.list

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Stable
import com.matejdro.micropebble.apps.ui.errors.InvalidPbwFileException
import com.matejdro.micropebble.appstore.api.ApiClient
import com.matejdro.micropebble.appstore.api.AppInstallSource
import com.matejdro.micropebble.appstore.api.AppInstallationClient
import com.matejdro.micropebble.appstore.api.AppStatus
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.common.exceptions.LibPebbleError
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.WatchappListKey
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import io.rebble.libpebblecommon.connection.Errors
import io.rebble.libpebblecommon.connection.LockerApi
import io.rebble.libpebblecommon.connection.UserFacingError
import io.rebble.libpebblecommon.locker.AppType
import io.rebble.libpebblecommon.locker.LockerWrapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.files.Path
import okio.buffer
import okio.sink
import okio.source
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.io.File
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

@Stable
@Inject
@ContributesScopedService
class WatchappListViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val lockerApi: LockerApi,
   private val context: Context,
   private val errorHandler: Errors,
   private val installationClient: AppInstallationClient,
   private val appstoreSourceService: AppstoreSourceService,
   private val api: ApiClient,
) : SingleScreenViewModel<WatchappListKey>(resources.scope) {
   private val _apps = MutableStateFlow<Outcome<WatchappListState>>(Outcome.Progress())
   private val _actionStatus = MutableStateFlow<Outcome<Unit>>(Outcome.Success(Unit))
   val actionStatus: StateFlow<Outcome<Unit>> = _actionStatus

   val installationSources = installationClient.appInstallSources

   private val _appUpdatingStatus = MutableStateFlow(emptyMap<Uuid, AppStatus>())
   val uiState =
      combineTransform(
         _apps,
         installationSources,
         appstoreSourceService.sources,
      ) { apps, installSources, sources ->
         emit(apps)
         if (apps is Outcome.Success) {
            val appsWithStatusUpdate = apps.data.map {
               val appId = it.app.properties.id
               val installSource = installSources[appId]
               val status = installationClient.isAppUpdatable(installSource, sources)
               it.copy(appStatus = status)
            }
            emit(Outcome.Success(appsWithStatusUpdate))
         }
      }.combine(_appUpdatingStatus) { state, allStatus ->
         if (allStatus.isNotEmpty()) {
            state.mapData { state ->
               state.map { it.copy(appStatus = allStatus[it.app.properties.id] ?: it.appStatus) }
            }
         } else {
            state
         }
      }

   val appstoreSources = appstoreSourceService.sources.map { it.filter { s -> s.enabled } }

   override fun onServiceRegistered() {
      actionLogger.logAction { "WatchappListViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_apps) {
         emitAll(
            combine(
               lockerApi.getLocker(AppType.Watchface, null, Int.MAX_VALUE),
               lockerApi.getLocker(AppType.Watchapp, null, Int.MAX_VALUE),
            ) { watchfaces, watchapps ->
               val mapWatch = fun List<LockerWrapper>.() = map {
                  WatchappListApp(it, AppStatus.CheckingForUpdates)
               }
               Outcome.Success(
                  WatchappListState(
                     watchfaces.filterIsInstance<LockerWrapper.NormalApp>().mapWatch(),
                     watchapps.mapWatch()
                  )
               )
            }
         )
      }
   }

   fun startInstall(contentUri: Uri) = resources.launchResourceControlTask(_actionStatus) {
      actionLogger.logAction { "WatchappListViewModel.startInstall($contentUri)" }

      withDefault {
         if (getFileName(contentUri)?.endsWith(".pbw") != true) {
            throw InvalidPbwFileException()
         }

         val tmpFile = File.createTempFile("pbw", null)
         val stream =
            requireNotNull(context.contentResolver.openInputStream(contentUri)) {
               "Files provider should not return null streams"
            }

         tmpFile.sink().use { fileSink ->
            stream.source().buffer().use { it.readAll(fileSink) }
         }

         try {
            runLibPebbleActionWithErrorConversion<UserFacingError.FailedToSideloadApp> {
               lockerApi.sideloadApp(Path(tmpFile.absolutePath))
            }
         } finally {
            tmpFile.delete()
            Unit // Extra Unit to not trip up detekt
         }

         emit(Outcome.Success(Unit))
      }
   }

   fun deleteApp(uuid: Uuid) = resources.launchResourceControlTask(_actionStatus) {
      actionLogger.logAction { "WatchappListViewModel.deleteApp(uuid = $uuid)" }

      withDefault {
         runLibPebbleActionWithErrorConversion<UserFacingError.FailedToRemovePbwFromLocker> {
            installationClient.uninstall(uuid)
         }

         emit(Outcome.Success(Unit))
      }
   }

   fun updateApp(uuid: Uuid) {
      actionLogger.logAction { "WatchappListViewModel.updateApp($uuid)" }
      resources.launchWithExceptionReporting {
         _appUpdatingStatus.emit(_appUpdatingStatus.value + (uuid to AppStatus.Updating))
         val sources = installationSources.first()
         val result =
            sources[uuid]?.let { asyncUpdateApp(it) } ?: Outcome.Error(UnknownCauseException("No app update source"))
         val status = if (result is Outcome.Error) {
            AppStatus.UpdateFailed(result.exception)
         } else {
            AppStatus.JustUpdated
         }
         _appUpdatingStatus.emit(_appUpdatingStatus.value + (uuid to status))
         delay(10.seconds)
         _appUpdatingStatus.emit(_appUpdatingStatus.value - uuid)
      }
   }

   private suspend fun asyncUpdateApp(installSource: AppInstallSource): Outcome<Unit> {
      val updateSource = appstoreSourceService.sources.first().findFor(installSource)
         ?: return Outcome.Error(UnknownCauseException("No app update source"))
      val appListing = api.fetchAppListing(updateSource, installSource)
         ?: return Outcome.Error(UnknownCauseException("Failed to fetch app listing"))
      return installationClient.install(appListing.latestRelease.pbwFile, installSource)
   }

   fun reorderApp(uuid: Uuid, index: Int) = resources.launchResourceControlTask(_actionStatus) {
      actionLogger.logAction { "WatchappListViewModel.reorderApp($uuid, $index)" }
      lockerApi.setAppOrder(uuid, index)

      emit(Outcome.Success(Unit))
   }

   fun changeAppInstallSource(appId: Uuid, newAppstoreId: Uuid) {
      resources.launchWithExceptionReporting {
         actionLogger.logAction { "WatchappListViewModel.changeAppInstallSource($appId, $newAppstoreId)" }
         val installSource = installationClient.appInstallSources.first()[appId] ?: error("Install source not found for $appId")
         installationClient.updateSources(appId, installSource.copy(sourceId = newAppstoreId))
      }
   }

   private suspend inline fun <reified E : UserFacingError> runLibPebbleActionWithErrorConversion(
      crossinline action: suspend () -> Boolean,
   ) {
      coroutineScope {
         val errorCollection = errorHandler.userFacingErrors.filterIsInstance<E>()
            .buffer(Channel.BUFFERED)
            .produceIn(this)

         val success = action()

         if (!success) {
            val error = withTimeoutOrNull(1.seconds) {
               errorCollection.consume { receive() }
            }
               ?.let { LibPebbleError(it.message) }

            if (error != null) {
               throw error
            }
         }

         errorCollection.cancel()
      }
   }

   private suspend fun getFileName(uri: Uri): String? = withDefault {
      val projection = arrayOf<String?>(MediaStore.MediaColumns.DISPLAY_NAME)
      context.contentResolver.query(uri, projection, null, null, null).use {
         if (it?.moveToFirst() != true) return@use null
         it.getString(0)
      }
   }

   private fun List<AppstoreSource>.findFor(installSource: AppInstallSource) =
      find { it.enabled && it.id == installSource.sourceId }
}
