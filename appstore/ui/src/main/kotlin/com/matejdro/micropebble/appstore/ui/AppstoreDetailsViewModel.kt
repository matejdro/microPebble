package com.matejdro.micropebble.appstore.ui

import com.matejdro.micropebble.appstore.api.ApiClient
import com.matejdro.micropebble.appstore.api.AppInstallState
import com.matejdro.micropebble.appstore.api.AppInstallationClient
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.collection.AppstoreCollectionPage
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.common.util.joinUrls
import com.matejdro.micropebble.navigation.keys.AppstoreDetailsScreenKey
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.rebble.libpebblecommon.connection.LockerApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.net.URL

@Inject
@ContributesScopedService
class AppstoreDetailsViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val lockerApi: LockerApi,
   private val installer: AppInstallationClient,
   private val api: ApiClient,
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

      emit(installer.install(pbwUrl))
   }
}
