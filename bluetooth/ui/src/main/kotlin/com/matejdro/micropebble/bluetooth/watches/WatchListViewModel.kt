package com.matejdro.micropebble.bluetooth.watches

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.bluetooth.api.ConnectionServiceStarter
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.WatchListKey
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.ActiveDevice
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.ConnectingPebbleDevice
import io.rebble.libpebblecommon.connection.KnownPebbleDevice
import io.rebble.libpebblecommon.connection.Watches
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class WatchListViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val watches: Watches,
   private val serviceStarter: ConnectionServiceStarter,
) : SingleScreenViewModel<WatchListKey>(resources.scope) {
   private val _state = MutableStateFlow<Outcome<WatchListState>>(Outcome.Progress())
   val state: StateFlow<Outcome<WatchListState>> = _state

   override fun onServiceRegistered() {
      actionLogger.logAction { "WatchListViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_state) {
         emitAll(
            watches.watches.onEach { deviceList ->
               if (deviceList.any { it is ConnectedPebbleDevice || it is ConnectingPebbleDevice }) {
                  serviceStarter.start()
               }
            }.map {
               Outcome.Success(WatchListState(it.filterIsInstance<KnownPebbleDevice>()))
            }
         )
      }
   }

   fun setDeviceConnect(knownDevice: KnownPebbleDevice, connect: Boolean) {
      actionLogger.logAction { "WatchListViewModel.setDeviceConnect(knownDevice = ${knownDevice.name}, connect = $connect)" }
      if (connect) {
         knownDevice.connect()
      } else {
         (knownDevice as? ActiveDevice)?.disconnect()
      }
   }

   fun forgetDevice(knownDevice: KnownPebbleDevice) {
      actionLogger.logAction { "WatchListViewModel.forgetDevice(knownDevice = ${knownDevice.name})" }
      knownDevice.forget()
   }
}
