package com.matejdro.micropebble.appstore.ui

import com.matejdro.micropebble.appstore.api.ApiClient
import com.matejdro.micropebble.appstore.api.AppInstallSource
import com.matejdro.micropebble.appstore.api.AppInstallState
import com.matejdro.micropebble.appstore.api.AppInstallationClient
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.collection.AppstoreCollectionPage
import com.matejdro.micropebble.appstore.ui.common.isCompatibleWith
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.common.util.joinUrls
import com.matejdro.micropebble.navigation.keys.AppstoreDetailsScreenKey
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.LockerApi
import io.rebble.libpebblecommon.connection.Watches
import io.rebble.libpebblecommon.metadata.WatchType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.net.URL
import kotlin.collections.first

@Inject
@ContributesScopedService
class AppstoreDetailsViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val lockerApi: LockerApi,
   private val installer: AppInstallationClient,
   private val api: ApiClient,
   private val watches: Watches,
) : SingleScreenViewModel<AppstoreDetailsScreenKey>(resources.scope) {
   val platform by lazy { key.platformFilter?.let { WatchType.fromCodename(it) } }
   private val _appState = MutableStateFlow<Outcome<AppInstallState>>(Outcome.Progress())
   val appState: StateFlow<Outcome<AppInstallState>> = _appState

   private val _appDataState = MutableStateFlow<Outcome<Application>>(Outcome.Progress())
   val appDataState: StateFlow<Outcome<Application>> = _appDataState

   val unofficialSupportedPlatforms by lazy {
      WatchType.entries.filter {
         it.getCompatibleAppVariants().any { variant -> key.app.isCompatibleWith(variant) }
      }
   }

   override fun onServiceRegistered() {
      actionLogger.logAction { "AppstoreDetailsViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_appState) {
         emit(Outcome.Progress())
         val connectedWatch = watches.watches.mapNotNull { watchList ->
            watchList.filterIsInstance<ConnectedPebbleDevice>().takeIf { it.isNotEmpty() }
         }.first().first()
         val isAppCompatible =
            connectedWatch.watchType.watchType.getCompatibleAppVariants()
               .any { key.app.isCompatibleWith(it) }
         if (!isAppCompatible) {
            emit(Outcome.Success(AppInstallState.INCOMPATIBLE))
         } else {
            emit(
               if (lockerApi.getLockerApp(key.app.uuid).first() != null) {
                  Outcome.Success(AppInstallState.INSTALLED)
               } else {
                  Outcome.Success(AppInstallState.CAN_INSTALL)
               }
            )
         }
      }

      val source = key.appstoreSource
      if (key.onlyPartialData && source != null) {
         resources.launchResourceControlTask(_appDataState) {
            val realData =
               api.http
                  .get(source.url.joinUrls("/v1/apps/id/${key.app.id}"))
                  .body<AppstoreCollectionPage>().apps.first()
            emit(Outcome.Success(realData))
         }
      } else {
         _appDataState.value = Outcome.Success(key.app)
      }
   }

   fun install() = resources.launchResourceControlTask(_appState) {
      actionLogger.logAction { "AppstoreDetailsViewModel.install()" }
      val app = appDataState.value
      if (app !is Outcome.Success) {
         return@launchResourceControlTask
      }
      val pbwUrl = URL(app.data.latestRelease.pbwFile)

      emit(Outcome.Progress())
      val outcome = installer.install(
         pbwUrl,
         key.appstoreSource?.let { AppInstallSource(app.data.uuid, app.data.id, it.id) }
      )
      emit(outcome.mapData { AppInstallState.INSTALLED })
   }

   fun uninstall() = resources.launchResourceControlTask(_appState) {
      actionLogger.logAction { "AppstoreDetailsViewModel.install()" }
      val app = appDataState.value
      if (app !is Outcome.Success) {
         return@launchResourceControlTask
      }
      emit(Outcome.Progress())
      installer.uninstall(app.data.uuid)
      emit(Outcome.Success(AppInstallState.CAN_INSTALL))
   }
}
