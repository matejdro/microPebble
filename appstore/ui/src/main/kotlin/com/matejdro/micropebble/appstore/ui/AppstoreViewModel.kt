package com.matejdro.micropebble.appstore.ui

import com.matejdro.micropebble.appstore.api.client.AppstoreClient
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Inject
@ContributesScopedService
class AppstoreViewModel(
   private val resources: CoroutineResourceManager,
   private val appstoreClient: AppstoreClient,
   private val actionLogger: ActionLogger,
) : SingleScreenViewModel<AppstoreScreenKey>(resources.scope) {
   private val _loadingState = MutableStateFlow<Outcome<AppstoreHomePage>>(Outcome.Progress())
   val loadingState: StateFlow<Outcome<AppstoreHomePage>> = _loadingState

   fun reloadHomePage() = resources.launchResourceControlTask(_loadingState) {
      actionLogger.logAction { "AppstoreViewModel.reloadHomePage()" }
      try {
         val result = appstoreClient.getHomePage(ApplicationType.Watchface)
         emit(Outcome.Success(result))
      } catch (e: IllegalArgumentException) {
         emit(Outcome.Error(DataParsingException(e.message, e)))
      }
   }
}
