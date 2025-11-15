package com.matejdro.micropebble.bluetooth.firmware

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matejdro.micropebble.bluetooth.errors.bluetoothUserFriendlyErrorMessage
import com.matejdro.micropebble.bluetooth.ui.R
import com.matejdro.micropebble.navigation.keys.FirmwareUpdateScreenKey
import com.matejdro.micropebble.navigation.keys.common.InputFile
import com.matejdro.micropebble.ui.components.ErrorAlertDialog
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import io.rebble.libpebblecommon.connection.FakeConnectedDevice
import io.rebble.libpebblecommon.connection.PebbleBleIdentifier
import io.rebble.libpebblecommon.connection.endpointmanager.FirmwareUpdater
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class UpdateFirmwareScreen(
   private val viewModel: UpdateFirmwareViewModel,
) : Screen<FirmwareUpdateScreenKey>() {
   @Composable
   override fun Content(key: FirmwareUpdateScreenKey) {
      val state = viewModel.watchInfo.collectAsStateWithLifecycleAndBlinkingPrevention().value
      val updateState = viewModel.updateStatus.collectAsStateWithLifecycle()

      val context = LocalContext.current

      val selectPwbResult = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { pbzUri ->
         if (pbzUri != null) {
            viewModel.selectPbz(pbzUri)
         }
      }

      ProgressErrorSuccessScaffold(
         state,
         Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
      ) { updateFirmwareState ->
         FirmwareUpdateScreenContent(
            updateFirmwareState,
            updateState::value,
            {
               context.startActivity(Intent(Intent.ACTION_VIEW, it))
            },
            {
               if (updateFirmwareState.pendingFirmware == null) {
                  selectPwbResult.launch(arrayOf("*/*"))
               } else {
                  viewModel.startInstall()
               }
            }
         )
      }
   }
}

@Composable
private fun FirmwareUpdateScreenContent(
   watchInfo: UpdateFirmwareState,
   updateStateGetter: () -> Outcome<Unit?>?,
   openBrowser: (Uri) -> Unit,
   start: () -> Unit,
) {
   Column(
      modifier = Modifier
         .fillMaxSize()
         .verticalScroll(rememberScrollState())
         .padding(16.dp)
         .safeDrawingPadding()
   ) {
      val watch = watchInfo.watch
      Text(watch.displayName())
      Text("${watch.watchType.watchType.codename} ${watch.watchType.revision}")
      Text(
         stringResource(R.string.current_firmware, watch.watchInfo.runningFwVersion.stringVersion),
         Modifier.padding(bottom = 16.dp)
      )

      Text(stringResource(R.string.you_can_get_firmware_files_from), Modifier.padding(bottom = 8.dp))

      Text(stringResource(R.string.original_pebble_watches))
      LinkText("https://github.com/bmacphail/pebblefw", openBrowser)

      Text(stringResource(R.string.core_watches))
      LinkText("https://github.com/coredevices/PebbleOS/releases", openBrowser, Modifier.padding(bottom = 32.dp))

      val updateState = updateStateGetter()
      ErrorAlertDialog(updateState, errorText = { it.bluetoothUserFriendlyErrorMessage() })

      if (updateState is Outcome.Progress) {
         val progress = updateState.progress
         if (progress == null) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
         } else {
            LinearProgressIndicator(
               { progress },
               Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp)
            )
         }
      } else {
         if (updateState?.data == null) {
            val pendingFirmware = watchInfo.pendingFirmware
            if (pendingFirmware == null) {
               Button(
                  onClick = { start() },
                  Modifier.align(Alignment.CenterHorizontally)
               ) { Text(stringResource(R.string.select_pbz_file)) }
            } else {
               Text(
                  stringResource(R.string.selected_firmware, pendingFirmware.filename),
                  Modifier.padding(bottom = 8.dp)
               )
               Button(
                  onClick = { start() },
                  Modifier.align(Alignment.CenterHorizontally),
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
               ) { Text(stringResource(R.string.start_installation)) }
            }
         } else {
            Text(stringResource(R.string.update_completed), Modifier.align(Alignment.CenterHorizontally))
         }
      }
   }
}

@Composable
private fun LinkText(url: String, openBrowser: (Uri) -> Unit, modifier: Modifier = Modifier) {
   Text(
      url,
      modifier
         .padding(bottom = 16.dp)
         .clickable(onClick = { openBrowser(url.toUri()) })
         .padding(4.dp),
      textDecoration = TextDecoration.Underline
   )
}

@Preview
@Composable
internal fun FirmwareUpdateNotSelectedPreview() {
   PreviewTheme {
      FirmwareUpdateScreenContent(
         UpdateFirmwareState(
            FakeConnectedDevice(
               PebbleBleIdentifier(""),
               null,
               FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle(),
               "My Watch 123",
               null,
               connectionFailureInfo = null,
               watchType = WatchHardwarePlatform.PEBBLE_ONE_EV_1
            ),
         ),
         { Outcome.Success(null) },
         {},
         {},
      )
   }
}

@FullScreenPreviews
@Composable
internal fun FirmwareUpdatePendingPreview() {
   PreviewTheme {
      FirmwareUpdateScreenContent(
         UpdateFirmwareState(
            FakeConnectedDevice(
               PebbleBleIdentifier(""),
               null,
               FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle(),
               "My Watch 123",
               null,
               connectionFailureInfo = null,
               watchType = WatchHardwarePlatform.PEBBLE_ONE_EV_1

            ),
            pendingFirmware = InputFile("content://folder/my_firmware.pbz".toUri(), "my_firmware.pbz")
         ),
         { Outcome.Success(null) },
         {},
         {},
      )
   }
}

@Preview
@Composable
internal fun FirmwareUpdateCompletePreview() {
   PreviewTheme {
      FirmwareUpdateScreenContent(
         UpdateFirmwareState(
            FakeConnectedDevice(
               PebbleBleIdentifier(""),
               null,
               FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle(),
               "My Watch 123",
               null,
               connectionFailureInfo = null,
               watchType = WatchHardwarePlatform.PEBBLE_ONE_EV_1

            ),
            pendingFirmware = InputFile("content://folder/my_firmware.pbz".toUri(), "my_firmware.pbz")
         ),
         { Outcome.Success(Unit) },
         {},
         {},
      )
   }
}

@Preview
@Composable
internal fun FirmwareUpdateProgressPreview() {
   PreviewTheme {
      FirmwareUpdateScreenContent(
         UpdateFirmwareState(
            FakeConnectedDevice(
               PebbleBleIdentifier(""),
               null,
               FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle(),
               "My Watch 123",
               null,
               connectionFailureInfo = null,
               watchType = WatchHardwarePlatform.PEBBLE_ONE_EV_1

            ),
            pendingFirmware = InputFile("content://folder/my_firmware.pbz".toUri(), "my_firmware.pbz")
         ),
         { Outcome.Progress(progress = 0.5f) },
         {},
         {},
      )
   }
}
