package com.matejdro.micropebble.apps.ui.developerconnection

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Stable
import androidx.core.content.getSystemService
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.DeveloperConnectionScreenKey
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.ConnectedPebble
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.Watches
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.net.NetworkInterface

@Stable
@Inject
@ContributesScopedService
class DeveloperConnectionScreenViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val watches: Watches,
   private val context: Context,
) : SingleScreenViewModel<DeveloperConnectionScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<DeveloperConnectionScreenState>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<DeveloperConnectionScreenState>> = _uiState

   private val selectedWatch by savedFlow { "" }

   private var currentDevConn: ConnectedPebble.DevConnection? = null
   override fun onServiceRegistered() {
      actionLogger.logAction { "DeveloperConnectionScreenViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_uiState) {
         val watchesFlow = watches.watches.map { it.filterIsInstance<ConnectedPebbleDevice>() }.distinctUntilChanged()

         val dataFlow = watchesFlow.flatMapLatest { connectedWatches ->
            if (connectedWatches.isEmpty()) {
               flowOf(Outcome.Success(DeveloperConnectionScreenState()))
            } else {
               selectedWatch.flatMapLatest { savedSelectedWatch ->
                  val watchesUiModels = connectedWatches.map { DeveloperConnectionScreenState.Watch(it.name, it.serial) }
                  val currentlySelectedWatchSerial = savedSelectedWatch
                     .takeIf { selectedWatchSerial -> watchesUiModels.any { it.serial == selectedWatchSerial } }
                     ?: watchesUiModels.first().serial

                  val currentlySelectedWatch = connectedWatches.first { it.serial == currentlySelectedWatchSerial }
                  currentDevConn = currentlySelectedWatch

                  combine(currentlySelectedWatch.devConnectionActive, getWlanIp()) { devConnectionActive, wlanIp ->
                     Outcome.Success(
                        DeveloperConnectionScreenState(
                           watchesUiModels,
                           currentlySelectedWatchSerial,
                           devConnectionActive,
                           wlanIp
                        )
                     )
                  }
               }
            }
         }

         emitAll(dataFlow)
      }
   }

   fun startDevConn() = resources.launchWithExceptionReporting {
      actionLogger.logAction { "DeveloperConnectionScreenViewModel.startDevConn()" }
      currentDevConn?.startDevConnection()
   }

   fun stopDevConn() = resources.launchWithExceptionReporting {
      actionLogger.logAction { "DeveloperConnectionScreenViewModel.stopDevConn()" }
      currentDevConn?.stopDevConnection()
   }

   fun selectDevice(serial: String) = resources.launchWithExceptionReporting {
      actionLogger.logAction { "DeveloperConnectionScreenViewModel.selectDevice(serial = $serial)" }
      currentDevConn?.stopDevConnection()
      selectedWatch.value = serial
   }

   private fun getWlanIp(): Flow<String> {
      val connectivityManager = context.getSystemService<ConnectivityManager>()!!
      val wlanInterface = NetworkInterface.getNetworkInterfaces().asSequence().firstOrNull { it.name.contains("wlan") }
         ?: return flowOf("")

      val networkChangesFlow = channelFlow<Unit> {
         val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

         val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
               trySend(Unit)
            }

            override fun onLost(network: Network) {
               trySend(Unit)
            }
         }
         connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

         awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
         }
      }.onStart { emit(Unit) }

      return networkChangesFlow.map { _ ->
         wlanInterface.inetAddresses.toList().mapNotNull { it.hostAddress }.joinToString("\n")
      }.distinctUntilChanged()
   }
}
