package com.matejdro.micropebble.apps.ui.list

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.WatchappListKey
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.LockerApi
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
class WatchappListViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val lockerApi: LockerApi,
) : SingleScreenViewModel<WatchappListKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<WatchappListState>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<WatchappListState>> = _uiState

   override fun onServiceRegistered() {
      actionLogger.logAction { "WatchappListViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_uiState) {
         emitAll(
            lockerApi.getAllLockerBasicInfo().map { Outcome.Success(WatchappListState(it)) }
         )
      }
   }
}
