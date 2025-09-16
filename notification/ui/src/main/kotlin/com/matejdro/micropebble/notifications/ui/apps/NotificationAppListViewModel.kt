package com.matejdro.micropebble.notifications.ui.apps

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.NotificationAppListKey
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.NotificationApps
import io.rebble.libpebblecommon.database.dao.AppWithCount
import io.rebble.libpebblecommon.database.entity.MuteState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class NotificationAppListViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val notificationApps: NotificationApps,
) : SingleScreenViewModel<NotificationAppListKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<List<AppWithCount>>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<List<AppWithCount>>> = _uiState

   override fun onServiceRegistered() {
      actionLogger.logAction { "NotificationAppListViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_uiState) {
         emitAll(notificationApps.notificationApps().map { Outcome.Success(it) })
      }
   }

   fun setAppEnabled(packageName: String, enabled: Boolean) {
      actionLogger.logAction { "NotificationAppListViewModel.toggleAppEnabled(packageName = $packageName, enabled = $enabled)" }

      notificationApps.updateNotificationAppMuteState(packageName, if (enabled) MuteState.Never else MuteState.Always)
   }
}
