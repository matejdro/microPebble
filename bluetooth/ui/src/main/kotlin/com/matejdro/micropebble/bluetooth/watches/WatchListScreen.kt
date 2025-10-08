package com.matejdro.micropebble.bluetooth.watches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.bluetooth.ui.R
import com.matejdro.micropebble.bluetooth.watches.fakes.FakeDisconnectedKnownDevice
import com.matejdro.micropebble.bluetooth.watches.fakes.FakeKnownConnectingDevice
import com.matejdro.micropebble.navigation.keys.BluetoothScanScreenKey
import com.matejdro.micropebble.navigation.keys.WatchListKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.ConnectingPebbleDevice
import io.rebble.libpebblecommon.connection.FakeConnectedDevice
import io.rebble.libpebblecommon.connection.KnownPebbleDevice
import io.rebble.libpebblecommon.connection.PebbleBleIdentifier
import io.rebble.libpebblecommon.connection.endpointmanager.FirmwareUpdater
import io.rebble.libpebblecommon.metadata.WatchColor
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import com.matejdro.micropebble.sharedresources.R as sharedR

@InjectNavigationScreen
@ContributesScreenBinding
class WatchListScreen(
   private val navigator: Navigator,
   private val viewModel: WatchListViewModel,
) : Screen<WatchListKey>() {
   @Composable
   override fun Content(key: WatchListKey) {
      val state = viewModel.state.collectAsStateWithLifecycleAndBlinkingPrevention().value

      Surface {
         ProgressErrorSuccessScaffold(state, Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            WatchListScreenContent(
               it,
               { navigator.navigateTo(BluetoothScanScreenKey) },
               viewModel::setDeviceConnect,
               viewModel::forgetDevice
            )
         }
      }
   }
}

@Composable
private fun WatchListScreenContent(
   state: WatchListState,
   startPairing: () -> Unit,
   setConnect: (KnownPebbleDevice, connect: Boolean) -> Unit,
   forget: (KnownPebbleDevice) -> Unit,
) {
   Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
      FlowRow(
         horizontalArrangement = Arrangement.spacedBy(8.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Button(onClick = startPairing) {
            Text(stringResource(R.string.pair_new_watch))
         }
      }

      Text(stringResource(R.string.paired_watches), Modifier.padding(16.dp))

      for (device in state.pairedDevices) {
         val deviceVariant = device.color
         val watchColor = deviceVariant?.color ?: MaterialTheme.colorScheme.surface
         val textColor: Color = if (watchColor.luminance() > LUMINANCE_HALF_BRIGHT) {
            Color.Black
         } else {
            Color.White
         }

         CompositionLocalProvider(LocalContentColor provides textColor) {
            val shape = RoundedCornerShape(8.dp)
            Column(
               Modifier
                  .padding(8.dp)
                  .fillMaxWidth()
                  .background(watchColor, shape)
                  .border(Dp.Hairline, MaterialTheme.colorScheme.onSurface, shape)
                  .padding(8.dp)
                  .clip(shape),
               verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
               Text(device.displayName())
               Text(deviceVariant?.uiDescription ?: "Unknown watch variant")

               Text(
                  if (device is ConnectedPebbleDevice) {
                     stringResource(R.string.connected)
                  } else if (device is ConnectingPebbleDevice) {
                     stringResource(sharedR.string.connecting)
                  } else {
                     stringResource(R.string.disconnected)
                  }
               )

               Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(stringResource(R.string.connect), Modifier.padding(end = 16.dp))
                  Switch(checked = device is ConnectingPebbleDevice || device is ConnectedPebbleDevice, onCheckedChange = {
                     setConnect(device, it)
                  })
               }

               Button(onClick = { forget(device) }) {
                  Text(stringResource(R.string.forget))
               }
            }
         }
      }
   }
}

private const val LUMINANCE_HALF_BRIGHT = 0.5

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun WatchListBlankPreview() {
   PreviewTheme {
      WatchListScreenContent(WatchListState(emptyList()), {}, { _, _ -> }, {})
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun WatchListWithDevicesPreview() {
   val deviceList = listOf(
      FakeConnectedDevice(
         PebbleBleIdentifier(""),
         null,
         FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle,
         "Red PT",
         null,
         color = WatchColor.TimeRed,
         connectionFailureInfo = null
      ),
      FakeConnectedDevice(
         PebbleBleIdentifier(""),
         null,
         FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle,
         "Black P2D",
         null,
         color = WatchColor.Pebble2DuoBlack,
         connectionFailureInfo = null
      ),
      FakeKnownConnectingDevice(
         name = "White P2D",
         color = WatchColor.Pebble2DuoWhite,
      ),
      FakeDisconnectedKnownDevice(
         name = "Classic Fly BLue",
         color = WatchColor.ClassicFlyBlue,
      ),
   )

   PreviewTheme {
      WatchListScreenContent(WatchListState(deviceList), {}, { _, _ -> }, {})
   }
}
