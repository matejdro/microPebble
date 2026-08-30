package com.matejdro.micropebble.bluetooth.firmware

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.matejdro.micropebble.webservices.api.GithubRelease
import io.rebble.libpebblecommon.connection.FakeConnectedDevice
import io.rebble.libpebblecommon.connection.FirmwareUpdateCheckState
import io.rebble.libpebblecommon.connection.PebbleBleIdentifier
import io.rebble.libpebblecommon.connection.endpointmanager.FirmwareUpdater
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import java.io.File
import androidx.compose.ui.platform.LocalContext
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@InjectNavigationScreen
class UpdateFirmwareScreen(
   private val viewModel: UpdateFirmwareViewModel,
) : Screen<FirmwareUpdateScreenKey>() {
   @Composable
   override fun Content(key: FirmwareUpdateScreenKey) {
      val state = viewModel.watchInfo.collectAsStateWithLifecycleAndBlinkingPrevention()
      val updateState = viewModel.updateStatus.collectAsStateWithLifecycle()
      val githubReleasesState = viewModel.githubReleases.collectAsStateWithLifecycle()
      val downloadProgressState = viewModel.downloadProgress.collectAsStateWithLifecycle()
      val isGithubAutoDownloadAvailable = viewModel.isGithubAutoDownloadAvailable

      val context = LocalContext.current

      val selectPwbResult = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { pbzUri ->
         if (pbzUri != null) {
            viewModel.selectPbz(pbzUri)
         }
      }

      ProgressErrorSuccessScaffold(
         state::value,
         Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
      ) { updateFirmwareState ->
         val githubReleases = githubReleasesState.value
         val downloadProgress = downloadProgressState.value
         
         FirmwareUpdateScreenContent(
            updateFirmwareState,
            updateState::value,
            githubReleases,
            downloadProgress,
            {
               context.startActivity(Intent(Intent.ACTION_VIEW, it))
            },
            {
               if (updateFirmwareState.pendingFirmware == null) {
                  selectPwbResult.launch(arrayOf("*/*"))
               } else {
                  viewModel.startInstall()
               }
            },
            onCheckGithubUpdates = { viewModel.checkGithubUpdates() },
            onDownloadFromGithub = { asset -> viewModel.downloadFromGithub(asset) },
            isGithubAutoDownloadAvailable = isGithubAutoDownloadAvailable
         )
      }
   }
}

@Composable
private fun FirmwareUpdateScreenContent(
   watchInfo: UpdateFirmwareState,
   updateStateGetter: () -> Outcome<Unit?>?,
   githubReleasesState: Outcome<List<GithubRelease>?>,
   downloadProgress: Outcome<File>,
   openBrowser: (Uri) -> Unit,
   start: () -> Unit,
   onCheckGithubUpdates: () -> Unit,
   onDownloadFromGithub: (com.matejdro.micropebble.webservices.api.GithubAsset) -> Unit,
   isGithubAutoDownloadAvailable: Boolean,
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

      // GitHub updates section - only available for asterix, obelix, getafix
      if (isGithubAutoDownloadAvailable) {
         Text(
            stringResource(R.string.github_releases),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
         )
         
         Button(
            onClick = onCheckGithubUpdates,
            Modifier
               .fillMaxWidth()
               .padding(bottom = 16.dp),
            enabled = githubReleasesState !is Outcome.Progress
         ) {
            if (githubReleasesState is Outcome.Progress) {
               CircularProgressIndicator(Modifier)
               Spacer(Modifier.height(8.dp))
               Text(stringResource(R.string.checking_github_updates))
            } else {
               Text(stringResource(R.string.check_github_updates))
            }
         }
      
         // Display GitHub releases if available
         if (githubReleasesState is Outcome.Success) {
            val releases = githubReleasesState.data
            // null = not checked yet, emptyList = checked and up to date
            if (releases == null) {
               // Initial state - no check done yet, don't show any message
            } else if (releases.isEmpty()) {
               Text(
                  stringResource(R.string.no_firmware_updates_available),
                  Modifier.padding(bottom = 16.dp)
               )
            } else {
               // Show releases with .pbz files
               val releasesWithPbz = releases.filter { it.pbzAssets.isNotEmpty() }
               if (releasesWithPbz.isEmpty()) {
                  Text(
                     stringResource(R.string.no_pbz_files_found),
                     Modifier.padding(bottom = 16.dp)
                  )
               } else {
                  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                     releasesWithPbz.forEach { release ->
                        GithubReleaseCard(
                           release = release,
                           onDownload = onDownloadFromGithub,
                           isDownloading = downloadProgress is Outcome.Progress
                        )
                     }
                  }
               }
            }
         } else if (githubReleasesState is Outcome.Error) {
            ErrorAlertDialog(
               githubReleasesState,
               errorText = { stringResource(R.string.github_api_error) },
               modifier = Modifier.padding(bottom = 16.dp)
            )
         }
      }
      
      // Download progress
      if (downloadProgress is Outcome.Progress) {
         LinearProgressIndicator(
            Modifier
               .fillMaxWidth()
               .padding(vertical = 16.dp)
         )
         Text(
            stringResource(R.string.downloading_firmware),
            Modifier.align(Alignment.CenterHorizontally)
         )
      } else if (downloadProgress is Outcome.Error) {
         ErrorAlertDialog(
            downloadProgress,
            errorText = { stringResource(R.string.firmware_download_failed) }
         )
      }

      // Installation section
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
                  Modifier.align(Alignment.CenterHorizontally),
                  enabled = githubReleasesState !is Outcome.Progress
               ) { Text(stringResource(R.string.select_pbz_file)) }
            } else {
               Text(
                  stringResource(R.string.selected_firmware, pendingFirmware?.filename ?: ""),
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
private fun GithubReleaseCard(
   release: GithubRelease,
   onDownload: (com.matejdro.micropebble.webservices.api.GithubAsset) -> Unit,
   isDownloading: Boolean,
) {
   Card(
      modifier = Modifier.fillMaxWidth(),
      onClick = {},
   ) {
      Column(modifier = Modifier.padding(16.dp)) {
         Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
         ) {
            Column {
               Text(
                  release.tag_name,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
               )
               release.name?.let {
                  Text(it, style = MaterialTheme.typography.bodyMedium)
               }
               release.published_at?.let {
                  val context = LocalContext.current
                  val locale = context.resources.configuration.locales.get(0)
                  Text(
                     DateTimeFormatter
                        .ofPattern("MMM dd, yyyy")
                        .withLocale(locale)
                        .format(ZonedDateTime.ofInstant(it, ZoneId.systemDefault())),
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.secondary
                  )
               }
            }
            if (release.is_prerelease) {
               Text(
                  "Pre-release",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.padding(start = 8.dp)
               )
            }
         }
         
         Spacer(Modifier.height(8.dp))
         
         // Show PBZ assets
         Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            release.pbzAssets.forEach { asset ->
               Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
               ) {
                  Column {
                     Text(asset.name, style = MaterialTheme.typography.bodyMedium)
                     Text(
                        "${asset.size / (1024 * 1024)} MB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                     )
                  }
                  TextButton(
                     onClick = { onDownload(asset) },
                     enabled = !isDownloading
                  ) {
                     Text(stringResource(R.string.download_firmware))
                  }
               }
            }
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
               FirmwareUpdateCheckState(false, null),
               FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle(),
               "My Watch 123",
               null,
               connectionFailureInfo = null,
               watchType = WatchHardwarePlatform.PEBBLE_ONE_EV_1
            ),
         ),
         { Outcome.Success(null) },
         Outcome.Success(null),
         Outcome.Success(File("")),
         {},
         {},
         {},
         { _ -> },
         isGithubAutoDownloadAvailable = false,
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
               FirmwareUpdateCheckState(false, null),
               FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle(),
               "My Watch 123",
               null,
               connectionFailureInfo = null,
               watchType = WatchHardwarePlatform.PEBBLE_ONE_EV_1

            ),
            pendingFirmware = InputFile("content://folder/my_firmware.pbz".toUri(), "my_firmware.pbz")
         ),
         { Outcome.Success(null) },
         Outcome.Success(null),
         Outcome.Success(File("")),
         {},
         {},
         {},
         { _ -> },
         isGithubAutoDownloadAvailable = false,
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
               FirmwareUpdateCheckState(false, null),
               FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle(),
               "My Watch 123",
               null,
               connectionFailureInfo = null,
               watchType = WatchHardwarePlatform.PEBBLE_ONE_EV_1

            ),
            pendingFirmware = InputFile("content://folder/my_firmware.pbz".toUri(), "my_firmware.pbz")
         ),
         { Outcome.Success(Unit) },
         Outcome.Success(null),
         Outcome.Success(File("")),
         {},
         {},
         {},
         { _ -> },
         isGithubAutoDownloadAvailable = false,
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
               FirmwareUpdateCheckState(false, null),
               FirmwareUpdater.FirmwareUpdateStatus.NotInProgress.Idle(),
               "My Watch 123",
               null,
               connectionFailureInfo = null,
               watchType = WatchHardwarePlatform.PEBBLE_ONE_EV_1

            ),
            pendingFirmware = InputFile("content://folder/my_firmware.pbz".toUri(), "my_firmware.pbz")
         ),
         { Outcome.Progress(progress = 0.5f) },
         Outcome.Success(null),
         Outcome.Success(File("")),
         {},
         {},
         {},
         { _ -> },
         isGithubAutoDownloadAvailable = false,
      )
   }
}
