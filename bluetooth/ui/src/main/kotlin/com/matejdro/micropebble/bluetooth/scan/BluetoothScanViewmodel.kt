package com.matejdro.micropebble.bluetooth.scan

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.base.BluetoothScanScreenKey
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.BleDiscoveredPebbleDevice
import io.rebble.libpebblecommon.connection.Scanning
import io.rebble.libpebblecommon.connection.Watches
import io.rebble.libpebblecommon.connection.bt.BluetoothState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class BluetoothScanViewmodel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val scanning: Scanning,
   private val watches: Watches,
) : SingleScreenViewModel<BluetoothScanScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<ScanState>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<ScanState>> = _uiState

   override fun onServiceRegistered() {
      actionLogger.logAction { "BluetoothScanViewmodel.onServiceRegistered()" }

      resources.launchResourceControlTask(_uiState) {
         val watchesFlow = watches.watches.map { watchList ->
            watchList.filterIsInstance<BleDiscoveredPebbleDevice>()
         }

         val bluetoothFlow = scanning.bluetoothEnabled.map { it == BluetoothState.Enabled }

         val scanningFlow = scanning.isScanningBle

         val finalFlow = combine(
            watchesFlow,
            scanningFlow,
            bluetoothFlow,
         ) { watchList, scanning, bluetoothOn ->
            ScanState(bluetoothOn, scanning, watchList)
         }.map {
            Outcome.Success(it)
         }

         emitAll(finalFlow)
      }
   }

   fun toggleScan() {
      actionLogger.logAction { "BluetoothScanViewmodel.toggleScan() ${scanning.isScanningBle.value}" }

      if (scanning.isScanningBle.value) {
         scanning.stopBleScan()
      } else {
         scanning.startBleScan()
      }
   }
}
