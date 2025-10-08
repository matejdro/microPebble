package com.matejdro.micropebble.notifications.ui.apps

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.matejdro.micropebble.navigation.keys.NotificationAppListKey
import com.matejdro.micropebble.notification.ui.R
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import io.rebble.libpebblecommon.database.MillisecondInstant
import io.rebble.libpebblecommon.database.dao.AppWithCount
import io.rebble.libpebblecommon.database.entity.MuteState
import io.rebble.libpebblecommon.database.entity.NotificationAppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import kotlin.time.Instant

@InjectNavigationScreen
@ContributesScreenBinding
class NotificationAppListScreen(
   private val viewModel: NotificationAppListViewModel,
) : Screen<NotificationAppListKey>() {
   @Composable
   override fun Content(key: NotificationAppListKey) {
      val state = viewModel.uiState.collectAsStateWithLifecycleAndBlinkingPrevention().value

      ProgressErrorSuccessScaffold(
         state,
         Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
      ) {
         NotificationAppListScreenContent(
            it,
            viewModel::setAppEnabled,
            viewModel::setNotificationsPhoneMute,
            viewModel::setCallsPhoneMute,
            viewModel::setRespectDoNotDisturb,
            viewModel::setSendNotifications
         )
      }
   }
}

@Composable
private fun NotificationAppListScreenContent(
   state: NotificationAppListState,
   setAppEnabled: (String, Boolean) -> Unit,
   setNotificationsPhoneMute: (Boolean) -> Unit,
   setCallsPhoneMute: (Boolean) -> Unit,
   setRespectDoNotDisturb: (Boolean) -> Unit,
   setSendNotifications: (Boolean) -> Unit,
) {
   LazyColumn(
      Modifier.fillMaxSize(),
      contentPadding = WindowInsets.safeDrawing.asPaddingValues()
   ) {
      item {
         Row(
            Modifier
               .fillMaxWidth()
               .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
         ) {
            Text(
               stringResource(R.string.send_notifications_to_the_watch),
               Modifier
                  .padding(end = 16.dp)
                  .weight(1f)
            )
            Switch(state.sendNotifications, onCheckedChange = setSendNotifications)
         }

         Row(
            Modifier
               .fillMaxWidth()
               .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
         ) {
            Text(
               stringResource(R.string.mute_phone_notifications_when_connected),
               Modifier
                  .padding(end = 16.dp)
                  .weight(1f)
            )
            Switch(state.mutePhoneNotificationSoundsWhenConnected, onCheckedChange = setNotificationsPhoneMute)
         }

         Row(
            Modifier
               .fillMaxWidth()
               .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
         ) {
            Text(
               stringResource(R.string.mute_phone_calls_when_connected),
               Modifier
                  .padding(end = 16.dp)
                  .weight(1f)
            )
            Switch(state.mutePhoneCallSoundsWhenConnected, onCheckedChange = setCallsPhoneMute)
         }

         Row(
            Modifier
               .fillMaxWidth()
               .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
         ) {
            Text(
               stringResource(R.string.respect_dnd),
               Modifier
                  .padding(end = 16.dp)
                  .weight(1f)
            )
            Switch(state.respectDoNotDisturb, onCheckedChange = setRespectDoNotDisturb)
         }
      }

      item { HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface) }

      itemsWithDivider(state.apps) { app ->
         App(app, setAppEnabled = { setAppEnabled(app.app.packageName, it) })
      }
   }
}

@Composable
private fun App(app: AppWithCount, setAppEnabled: (Boolean) -> Unit) {
   Row(
      Modifier
         .fillMaxSize()
         .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
   ) {
      AppIcon(app.app.packageName)

      Text(app.app.name)

      Spacer(Modifier.weight(1f))
      Switch(app.app.muteState == MuteState.Never, onCheckedChange = setAppEnabled)
   }
}

@Composable
private fun AppIcon(packageName: String) {
   if (LocalInspectionMode.current) {
      Box(
         Modifier
            .size(32.dp)
            .background(Color.Cyan),
      )

      return
   }

   Box(Modifier.size(32.dp), propagateMinConstraints = true) {
      val context = LocalContext.current

      var appIconDrawable by remember { mutableStateOf<Drawable?>(null) }
      Image(rememberDrawablePainter(appIconDrawable), contentDescription = null)

      LaunchedEffect(packageName) {
         @Suppress("InjectDispatcher") // This is never used in tests, so it can be hardcoded
         withContext(Dispatchers.Default) {
            try {
               val pm = context.packageManager
               appIconDrawable = pm.getApplicationIcon(packageName)
            } catch (ignored: PackageManager.NameNotFoundException) {
               appIconDrawable = null
            }
         }
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun NotificationAppListScreenContentPreview() {
   PreviewTheme {
      val apps = List(10) {
         AppWithCount(
            NotificationAppItem(
               "pkg.$it",
               "App $it",
               MuteState.Never,
               emptyList(),
               MillisecondInstant(Instant.DISTANT_PAST),
               MillisecondInstant(Instant.DISTANT_PAST)
            ),
            0
         )
      }

      val state = NotificationAppListState(
         apps,
         true,
         false,
         false,
         true
      )

      NotificationAppListScreenContent(
         state,
         { _, _ -> },
         {},
         {},
         {},
         {}
      )
   }
}
