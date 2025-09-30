package com.matejdro.micropebble.apps.ui.list

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.apps.ui.R
import com.matejdro.micropebble.apps.ui.errors.installUserFriendlyErrorMessage
import com.matejdro.micropebble.navigation.keys.WatchappListKey
import com.matejdro.micropebble.ui.components.ErrorAlertDialog
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import io.rebble.libpebblecommon.locker.AppBasicProperties
import io.rebble.libpebblecommon.locker.AppType
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import java.util.Locale
import kotlin.uuid.Uuid

@InjectNavigationScreen
class WatchappListScreen(
   private val viewModel: WatchappListViewModel,
) : Screen<WatchappListKey>() {
   @Composable
   override fun Content(key: WatchappListKey) {
      val state = viewModel.uiState.collectAsStateWithLifecycleAndBlinkingPrevention().value

      ProgressErrorSuccessScaffold(
         state,
         Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
      ) {
         val selectPwbResult = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { pbwUri ->
            if (pbwUri != null) {
               viewModel.startInstall(pbwUri)
            }
         }

         WatchappListScreenContent(
            it,
            viewModel.actionStatus.collectAsStateWithLifecycleAndBlinkingPrevention().value,
            installFromPbw = {
               selectPwbResult.launch(arrayOf("*/*"))
            },
            deleteApp = viewModel::deleteApp
         )
      }
   }
}

@Composable
private fun WatchappListScreenContent(
   state: WatchappListState,
   actionStatus: Outcome<Unit>?,
   installFromPbw: () -> Unit,
   deleteApp: (Uuid) -> Unit,
) {
   ErrorAlertDialog(actionStatus, errorText = { it.installUserFriendlyErrorMessage() })
   LazyColumn(
      Modifier.fillMaxSize(),
      contentPadding = WindowInsets.safeDrawing.asPaddingValues()
   ) {
      item {
         if (actionStatus is Outcome.Progress) {
            CircularProgressIndicator(Modifier.padding(8.dp))
         } else {
            Button(onClick = installFromPbw, Modifier.padding(8.dp)) {
               Text("Install from PBW")
            }
         }
      }

      item { HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface) }

      itemsWithDivider(state.apps) { app ->
         App(app, actionStatus !is Outcome.Progress) { deleteApp(app.id) }
      }
   }
}

@Composable
private fun App(app: AppBasicProperties, enableActions: Boolean, delete: () -> Unit) {
   Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
      Column(
         modifier = Modifier
            .weight(1f),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Text(app.title, style = MaterialTheme.typography.bodyMedium)
         Text(
            app.type.code.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.bodySmall
         )
      }

      OutlinedButton(onClick = delete, enabled = enableActions) {
         Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(R.string.delete_app))
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun NotificationAppListScreenContentPreview() {
   PreviewTheme {
      val apps = List(10) {
         AppBasicProperties(
            Uuid.fromLongs(0L, it.toLong()),
            if (it % 2 == 0) AppType.Watchapp else AppType.Watchface,
            "App $it",
            "Dev $it"
         )
      }

      val state = WatchappListState(apps)

      WatchappListScreenContent(state, Outcome.Success(Unit), {}, {})
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun NotificationAppListInstallingPreview() {
   PreviewTheme {
      val apps = List(10) {
         AppBasicProperties(
            Uuid.fromLongs(0L, it.toLong()),
            if (it % 2 == 0) AppType.Watchapp else AppType.Watchface,
            "App $it",
            "Dev $it"
         )
      }

      val state = WatchappListState(apps)

      WatchappListScreenContent(state, Outcome.Progress(Unit), {}, {})
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun NotificationAppListInstallingErrorPreview() {
   PreviewTheme {
      val apps = List(10) {
         AppBasicProperties(
            Uuid.fromLongs(0L, it.toLong()),
            if (it % 2 == 0) AppType.Watchapp else AppType.Watchface,
            "App $it",
            "Dev $it"
         )
      }

      val state = WatchappListState(apps)

      WatchappListScreenContent(state, Outcome.Error(NoNetworkException()), {}, {})
   }
}
