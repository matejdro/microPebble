package com.matejdro.micropebble.home

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.bluetooth.api.ConnectionServiceStarter
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.base.HomeScreenKey
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
class HomeViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val watches: Watches,
   private val serviceStarter: ConnectionServiceStarter,
) : SingleScreenViewModel<HomeScreenKey>(resources.scope) {
   private val _state = MutableStateFlow<Outcome<HomeState>>(Outcome.Progress())
   val state: StateFlow<Outcome<HomeState>> = _state

   override fun onServiceRegistered() {
      actionLogger.logAction { "HomeViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_state) {
         emitAll(
            watches.watches.onEach { deviceList ->
               if (deviceList.any { it is ConnectedPebbleDevice || it is ConnectingPebbleDevice }) {
                  serviceStarter.start()
               }
            }.map {
               Outcome.Success(HomeState(it.filterIsInstance<KnownPebbleDevice>()))
            }
         )
      }
   }

   fun setDeviceConnect(knownDevice: KnownPebbleDevice, connect: Boolean) {
      actionLogger.logAction { "HomeViewModel.setDeviceConnect(knownDevice = ${knownDevice.name}, connect = $connect)" }
      if (connect) {
         knownDevice.connect()
      } else {
         (knownDevice as? ActiveDevice)?.disconnect()
      }
   }

   fun forgetDevice(knownDevice: KnownPebbleDevice) {
      actionLogger.logAction { "HomeViewModel.forgetDevice(knownDevice = ${knownDevice.name})" }
      knownDevice.forget()
   }
}
