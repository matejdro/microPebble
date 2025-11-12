package com.matejdro.micropebble.bluetooth.watches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.bluetooth.ui.R
import com.matejdro.micropebble.bluetooth.watches.fakes.FakeDisconnectedKnownDevice
import com.matejdro.micropebble.bluetooth.watches.fakes.FakeKnownConnectingDevice
import com.matejdro.micropebble.navigation.keys.BluetoothScanScreenKey
import com.matejdro.micropebble.navigation.keys.FirmwareUpdateScreenKey
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
         ProgressErrorSuccessScaffold(state, Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) { state ->
            WatchListScreenContent(
               state = state,
               startPairing = { navigator.navigateTo(BluetoothScanScreenKey) },
               updateFirmware = { navigator.navigateTo(FirmwareUpdateScreenKey(it.serial)) },
               setConnect = viewModel::setDeviceConnect,
               forget = viewModel::forgetDevice,
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
   updateFirmware: (KnownPebbleDevice) -> Unit,
   forget: (KnownPebbleDevice) -> Unit,
) {
   Box {
      LazyColumn(
         verticalArrangement = Arrangement.spacedBy(32.dp), modifier = Modifier.fillMaxSize(),
         contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
      ) {
         items(state.pairedDevices, key = { it.serial }) { device ->
            Watch(device, setConnect, updateFirmware, forget)
         }

         item {
            // Extra padding at the end to allow scrolling past the FAB
            Box(Modifier.size(100.dp))
         }
      }

      if (state.pairedDevices.isEmpty()) {
         Text(
            stringResource(R.string.no_paired_watches_yet),
            Modifier
               .fillMaxSize()
               .wrapContentSize(Alignment.Center),
            style = MaterialTheme.typography.titleSmall
         )
      }

      FloatingActionButton(
         onClick = startPairing,
         Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp)
            .align(Alignment.BottomEnd)
      ) {
         Icon(painterResource(R.drawable.add), contentDescription = stringResource(R.string.pair_new_watch))
      }
   }
}

@Composable
private fun Watch(
   device: KnownPebbleDevice,
   setConnect: (KnownPebbleDevice, Boolean) -> Unit,
   updateFirmware: (KnownPebbleDevice) -> Unit,
   forget: (KnownPebbleDevice) -> Unit,
) {
   val deviceVariant = device.color
   val watchColor = deviceVariant?.color?.reduceSaturation() ?: MaterialTheme.colorScheme.surface
   val textColor: Color = if (watchColor.luminance() > LUMINANCE_HALF_BRIGHT) {
      Color.Black
   } else {
      Color.White
   }

   CompositionLocalProvider(LocalContentColor provides textColor) {
      val shape = RoundedCornerShape(8.dp)
      Column(
         Modifier
            .padding(horizontal = 32.dp)
            .width(300.dp)
            .background(watchColor, shape)
            .border(Dp.Hairline, MaterialTheme.colorScheme.onSurface, shape)
            .padding(16.dp)
            .clip(shape),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Text(device.displayName(), style = MaterialTheme.typography.titleMedium)
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

         if (device is ConnectedPebbleDevice) {
            Button(onClick = { updateFirmware(device) }) {
               Text(stringResource(R.string.update_firmwrare))
            }
         }

         Button(onClick = { forget(device) }) {
            Text(stringResource(R.string.forget))
         }
      }
   }
}

@Suppress("MagicNumber") // Array indexes
private fun Color.reduceSaturation(): Color {
   // Too high saturation stands out too much in contrast to the background. Reduce it, while still preserving the watch hue.

   val hslArray = FloatArray(3)
   ColorUtils.colorToHSL(toArgb(), hslArray)
   hslArray[1] = hslArray[1].coerceAtMost(0.5f)
   return Color(ColorUtils.HSLToColor(hslArray))
}

private const val LUMINANCE_HALF_BRIGHT = 0.5

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun WatchListBlankPreview() {
   PreviewTheme {
      WatchListScreenContent(WatchListState(emptyList()), {}, { _, _ -> }, {}, {})
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
         FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle(),
         "Red PT",
         null,
         serial = "1",
         color = WatchColor.TimeRed,
         connectionFailureInfo = null
      ),
      FakeConnectedDevice(
         PebbleBleIdentifier(""),
         null,
         FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle(),
         "Black P2D",
         null,
         serial = "2",
         color = WatchColor.Pebble2DuoBlack,
         connectionFailureInfo = null
      ),
      FakeKnownConnectingDevice(
         name = "White P2D",
         serial = "3",
         color = WatchColor.Pebble2DuoWhite,
      ),
      FakeDisconnectedKnownDevice(
         name = "Classic Fly BLue",
         serial = "4",
         color = WatchColor.ClassicFlyBlue,
      ),
   )

   PreviewTheme {
      WatchListScreenContent(WatchListState(deviceList), {}, { _, _ -> }, {}, {})
   }
}
