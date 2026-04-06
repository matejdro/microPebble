package com.matejdro.micropebble.apps.ui.webviewconfig

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.Watches
import io.rebble.libpebblecommon.js.PKJSApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import kotlin.time.Duration.Companion.seconds

@Stable
@Inject
@ContributesScopedService
class AppConfigScreenViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val watches: Watches,
) : SingleScreenViewModel<AppConfigScreenKey>(resources.scope) {
   private val _configUrl = MutableStateFlow<Outcome<AppConfigScreenState>>(Outcome.Progress())
   val configUrl: StateFlow<Outcome<AppConfigScreenState>> = _configUrl

   private var session: PKJSApp? = null

   override fun onServiceRegistered() {
      actionLogger.logAction { "AppConfigScreenViewModel.onServiceRegistered()" }
      resources.launchResourceControlTask(_configUrl) {
         val connectedWatch =
            watches.watches.mapNotNull { watchList ->
               watchList.filterIsInstance<ConnectedPebbleDevice>().takeIf { it.isNotEmpty() }
            }.first().first()
         connectedWatch.launchApp(key.uuid)

         val session = connectedWatch.currentCompanionAppSessions.mapNotNull { sessions ->
            sessions.filterIsInstance<PKJSApp>().firstOrNull()
               ?.takeIf { it.uuid == key.uuid }
               ?: return@mapNotNull null
         }.first()

         this@AppConfigScreenViewModel.session = session
         var requestConfigurationUrl: String? = session.requestConfigurationUrl()
         var retries = 0

         while (requestConfigurationUrl == null) {
            if (retries++ > URL_OPEN_RETRIES) {
               error("No URL. Did the app open?")
            }

            delay(1.seconds)
            requestConfigurationUrl = session.requestConfigurationUrl()
         }

         emit(Outcome.Success(AppConfigScreenState.WebView(requestConfigurationUrl)))
      }
   }

   fun save(data: String) {
      actionLogger.logAction { "AppConfigScreenViewModel.save()" }
      if (data.isNotBlank()) {
         session?.triggerOnWebviewClosed(data)
      }
      _configUrl.value = Outcome.Success(AppConfigScreenState.Close)
   }
}

sealed class AppConfigScreenState {
   data class WebView(val url: String) : AppConfigScreenState()
   object Close : AppConfigScreenState()
}

private const val URL_OPEN_RETRIES = 10
