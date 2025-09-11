package com.matejdro.micropebble.bluetooth.scan

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.matejdro.micropebble.bluetooth.ui.R
import com.matejdro.micropebble.navigation.keys.base.BluetoothScanScreenKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import io.rebble.libpebblecommon.connection.DiscoveredPebbleDevice
import io.rebble.libpebblecommon.connection.PebbleDevice
import io.rebble.libpebblecommon.connection.PebbleIdentifier
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
               pairToDevice = {}
            )
         }
      }
   }
}

@Composable
private fun ScanScreenContent(scanState: ScanState, toggleScan: () -> Unit, pairToDevice: (PebbleDevice) -> Unit) {
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
         Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(watch.name, modifier = Modifier.weight(1f))
            Button(onClick = { pairToDevice(watch) }) {
               Text(stringResource(R.string.pair))
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
      ScanScreenContent(ScanState(true, false, emptyList()), {}, {})
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScanStartedPreview() {
   PreviewTheme {
      ScanScreenContent(ScanState(true, true, emptyList()), {}, {})
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
            List(5) {
               object : DiscoveredPebbleDevice {
                  override val identifier: PebbleIdentifier
                     get() = throw UnsupportedOperationException("Not supported in fakes")
                  override val name: String
                     get() = "Watch $it"
                  override val nickname: String?
                     get() = name

                  override fun connect() {}
               }
            }
         ),
         {},
         {}
      )
   }
}
