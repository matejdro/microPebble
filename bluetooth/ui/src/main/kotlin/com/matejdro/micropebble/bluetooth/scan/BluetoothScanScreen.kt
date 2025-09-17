package com.matejdro.micropebble.bluetooth.scan

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.matejdro.micropebble.bluetooth.ui.R
import com.matejdro.micropebble.navigation.keys.base.BluetoothScanScreenKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.ConnectingPebbleDevice
import io.rebble.libpebblecommon.connection.ConnectionFailureInfo
import io.rebble.libpebblecommon.connection.DiscoveredPebbleDevice
import io.rebble.libpebblecommon.connection.FakeConnectedDevice
import io.rebble.libpebblecommon.connection.PebbleBleIdentifier
import io.rebble.libpebblecommon.connection.PebbleDevice
import io.rebble.libpebblecommon.connection.PebbleIdentifier
import io.rebble.libpebblecommon.connection.endpointmanager.FirmwareUpdater
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class BluetoothScanScreen(
   private val viewmodel: BluetoothScanViewmodel,
) : Screen<BluetoothScanScreenKey>() {
   @Composable
   override fun Content(key: BluetoothScanScreenKey) {
      val state = viewmodel.uiState.collectAsStateWithLifecycleAndBlinkingPrevention()
      Surface() {
         ProgressErrorSuccessScaffold(state.value) { scanState ->
            val turnOnBluetoothIntent = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
               if (it.resultCode == Activity.RESULT_OK) {
                  viewmodel.toggleScan()
               }
            }

            val bluetoothPermission = rememberMultiplePermissionsState(
               listOf(
                  android.Manifest.permission.BLUETOOTH_SCAN,
                  android.Manifest.permission.BLUETOOTH_CONNECT,
               )
            ) { permissions ->
               if (permissions.values.all { it }) {
                  if (!scanState.bluetoothOn) {
                     turnOnBluetoothIntent.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                  } else {
                     viewmodel.toggleScan()
                  }
               }
            }

            val context = LocalContext.current
            val companionManager = context.getSystemService<CompanionDeviceManager>()!!

            ScanScreenContent(
               scanState,
               toggleScan = {
                  if (!bluetoothPermission.allPermissionsGranted) {
                     bluetoothPermission.launchMultiplePermissionRequest()
                  } else if (!scanState.bluetoothOn) {
                     turnOnBluetoothIntent.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                  } else {
                     viewmodel.toggleScan()
                  }
               },
               pairToDevice = {
                  associateWithCompanionManager(companionManager, it, context)
               },
               cancelPairing = viewmodel::cancelPairing
            )
         }
      }
   }

   private fun associateWithCompanionManager(
      companionManager: CompanionDeviceManager,
      device: PebbleDevice,
      context: Context,
   ) {
      companionManager.associate(
         AssociationRequest.Builder().setSingleDevice(true)
            .addDeviceFilter(
               BluetoothDeviceFilter.Builder()
                  .setAddress(device.identifier.asString)
                  .build()
            ).build(),
         object : CompanionDeviceManager.Callback() {
            override fun onFailure(error: CharSequence?) {}

            override fun onAssociationPending(intentSender: IntentSender) {
               super.onAssociationPending(intentSender)
               context.startIntentSender(intentSender, null, 0, 0, 0)
            }

            override fun onAssociationCreated(associationInfo: AssociationInfo) {
               viewmodel.connect(device)
            }
         },
         null
      )
   }
}

@Composable
private fun ScanScreenContent(
   scanState: ScanState,
   toggleScan: () -> Unit,
   pairToDevice: (PebbleDevice) -> Unit,
   cancelPairing: (PebbleDevice) -> Unit,
) {
   Column(
      Modifier
         .verticalScroll(rememberScrollState())
         .windowInsetsPadding(WindowInsets.safeDrawing)
         .padding(horizontal = 16.dp)
   ) {
      Button(toggleScan) {
         val text = if (scanState.scanning) {
            stringResource(R.string.stop_scan)
         } else {
            stringResource(R.string.start_scan)
         }

         Text(text)
      }

      Text(stringResource(R.string.discovered_watches), modifier = Modifier.padding(vertical = 8.dp))

      for (watch in scanState.foundDevices) {
         Row(Modifier.defaultMinSize(minHeight = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(watch.name, modifier = Modifier.weight(1f))

            if (watch is ConnectingPebbleDevice) {
               Row {
                  CircularProgressIndicator(Modifier.size(24.dp))
                  Button(onClick = { cancelPairing(watch) }) {
                     Text(stringResource(R.string.cancel))
                  }
               }
            } else if (watch is ConnectedPebbleDevice) {
               Icon(
                  painterResource(R.drawable.outline_check_24),
                  contentDescription = stringResource(R.string.pairing_successful)
               )
            } else {
               Button(onClick = { pairToDevice(watch) }) {
                  Text(stringResource(R.string.pair))
               }
            }
         }
      }
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScanStoppedPreview() {
   PreviewTheme {
      ScanScreenContent(ScanState(true, false, emptyList()), {}, {}, {})
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScanStartedPreview() {
   PreviewTheme {
      ScanScreenContent(ScanState(true, true, emptyList()), {}, {}, {})
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScanStartedWithDevicesPreview() {
   PreviewTheme {
      ScanScreenContent(
         ScanState(
            true,
            true,
            listOf(
               object : DiscoveredPebbleDevice {
                  override val identifier: PebbleIdentifier
                     get() = throw UnsupportedOperationException("Not supported in fakes")
                  override val name: String
                     get() = "Discovered watch"
                  override val nickname: String?
                     get() = name
                  override val connectionFailureInfo: ConnectionFailureInfo?
                     get() = null

                  override fun connect() {}
               },
               object : ConnectingPebbleDevice {
                  override val identifier: PebbleIdentifier
                     get() = throw UnsupportedOperationException("Not supported in fakes")
                  override val name: String
                     get() = "Pairing watch"
                  override val nickname: String?
                     get() = name
                  override val negotiating: Boolean
                     get() = false
                  override val rebootingAfterFirmwareUpdate: Boolean
                     get() = false
                  override val connectionFailureInfo: ConnectionFailureInfo?
                     get() = null

                  override fun disconnect() {}

                  override fun connect() {}
               },
               FakeConnectedDevice(
                  PebbleBleIdentifier(""),
                  null,
                  FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle,
                  "Connected watch",
                  null,
                  connectionFailureInfo = null
               )
            )
         ),
         {},
         {},
         {}
      )
   }
}
