package com.matejdro.micropebble.apps.ui.webviewconfig

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.Watches
import io.rebble.libpebblecommon.js.PKJSApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.net.URLDecoder

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

         val session =
            connectedWatch.currentCompanionAppSession.filterNotNull().filterIsInstance<PKJSApp>().first { it.uuid == key.uuid }
         this@AppConfigScreenViewModel.session = session
         emit(Outcome.Success(AppConfigScreenState.WebView(session.requestConfigurationUrl() ?: error("No URL"))))
      }
   }

   fun save(data: String) {
      actionLogger.logAction { "AppConfigScreenViewModel.save()" }
      if (data.isNotBlank()) {
         session?.triggerOnWebviewClosed(URLDecoder.decode(data, "utf-8"))
      }
      _configUrl.value = Outcome.Success(AppConfigScreenState.Close)
   }
}

sealed class AppConfigScreenState {
   data class WebView(val url: String) : AppConfigScreenState()
   object Close : AppConfigScreenState()
}
