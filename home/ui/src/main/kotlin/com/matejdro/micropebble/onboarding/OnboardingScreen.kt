package com.matejdro.micropebble.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.matejdro.micropebble.home.ui.R
import com.matejdro.micropebble.navigation.keys.HomeScreenKey
import com.matejdro.micropebble.navigation.keys.OnboardingKey
import com.matejdro.micropebble.navigation.keys.WatchListKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.instructions.ReplaceBackstack
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class OnboardingScreen(
   private val viewModel: OnboardingScreenViewModel,
   private val navigator: Navigator,
) : Screen<OnboardingKey>() {
   @Composable
   override fun Content(key: OnboardingKey) {
      val state by viewModel.uiState.collectAsStateWithLifecycleAndBlinkingPrevention()

      ProgressErrorSuccessScaffold(
         state,
         Modifier
            .fillMaxSize()
            .safeDrawingPadding()
      ) {
         OnboardingContent(
            it,
            viewModel::requestNotificationPermissions,
            {
               navigator.navigate(
                  ReplaceBackstack(
                     HomeScreenKey(WatchListKey),
                  )
               )
            }
         )
      }
   }
}

@Composable
private fun OnboardingContent(
   state: OnboardingState,
   requestNotificationListenerPermission: () -> Unit,
   continueToApp: () -> Unit,
) {
   Column(
      Modifier
         .fillMaxSize()
         .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
   ) {
      Column(
         modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .weight(1f)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
         verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
         OnboardingScrollContent(state, requestNotificationListenerPermission)
      }

      Surface(
         modifier = Modifier
            .fillMaxWidth(),
         color = MaterialTheme.colorScheme.secondaryContainer,
         shadowElevation = 16.dp
      ) {
         Button(
            onClick = continueToApp,
            Modifier
               .wrapContentWidth()
               .padding(16.dp)
         ) {
            Text(stringResource(R.string.continue_to_the_app))
         }
      }
   }
}

@Composable
private fun ColumnScope.OnboardingScrollContent(state: OnboardingState, requestNotificationListenerPermission: () -> Unit) {
   Text(stringResource(R.string.onboarding_title))
   Text(stringResource(R.string.onboarding_intro))

   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      NotificationPermission()
   }
   ContactsPermission()
   LocationPermission()
   NotificationListenerPermission(state, requestNotificationListenerPermission)
   CalendarPermission()
   VoicePermission()
}

@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun NotificationPermission() {
   var rejectedPermission by remember { mutableStateOf(false) }
   val permissionState = rememberPermissionState(
      Manifest.permission.POST_NOTIFICATIONS,
   ) {
      if (!it) {
         rejectedPermission = true
      }
   }

   Card(Modifier.fillMaxWidth()) {
      Column(
         Modifier.padding(8.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Text(stringResource(R.string.notifications_permission_title), style = MaterialTheme.typography.headlineSmall)
         Text(stringResource(R.string.notification_permission_description))

         SinglePermissionButton(permissionState, rejectedPermission)
      }
   }
}

@Composable
private fun ContactsPermission() {
   var rejectedPermission by remember { mutableStateOf(false) }
   val permissionState = rememberPermissionState(
      Manifest.permission.READ_CONTACTS,
   ) {
      if (!it) {
         rejectedPermission = true
      }
   }

   Card(Modifier.fillMaxWidth()) {
      Column(
         Modifier.padding(8.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Text(stringResource(R.string.contacts_permission_title), style = MaterialTheme.typography.headlineSmall)
         Text(stringResource(R.string.contacts_permission_description))

         SinglePermissionButton(permissionState, rejectedPermission)
      }
   }
}

@Composable
private fun LocationPermission() {
   var rejectedPermission by remember { mutableStateOf(false) }
   val foregroundPermissionState = rememberMultiplePermissionsState(
      listOf(
         Manifest.permission.ACCESS_COARSE_LOCATION,
         Manifest.permission.ACCESS_FINE_LOCATION,
      )
   ) { permissions ->
      if (permissions.values.none { granted -> granted }) {
         rejectedPermission = true
      }
   }
   val backgroundPermissionState = rememberPermissionState(
      Manifest.permission.ACCESS_BACKGROUND_LOCATION,
   ) {
      if (!it) {
         rejectedPermission = true
      }
   }

   val context = LocalContext.current

   Card(Modifier.fillMaxWidth()) {
      Column(
         Modifier.padding(8.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Text(stringResource(R.string.location_permission_title), style = MaterialTheme.typography.headlineSmall)
         Text(stringResource(R.string.location_permission_description))

         if (foregroundPermissionState.allPermissionsGranted && backgroundPermissionState.status == PermissionStatus.Granted) {
            Text("✅")
         } else if (rejectedPermission) {
            Button(
               onClick = {
                  openSystemPermissionSettings(context)
               }
            ) { Text(stringResource(R.string.open_settings)) }
         } else if (foregroundPermissionState.permissions.none { it.status == PermissionStatus.Granted }) {
            Button(onClick = { foregroundPermissionState.launchMultiplePermissionRequest() }) {
               Text(stringResource(R.string.grant))
            }
         } else {
            Button(onClick = { backgroundPermissionState.launchPermissionRequest() }) {
               Text(stringResource(R.string.grant_background))
            }
         }
      }
   }
}

@Composable
private fun NotificationListenerPermission(onboardingState: OnboardingState, requestPermission: () -> Unit) {
   Card(Modifier.fillMaxWidth()) {
      Column(
         Modifier.padding(8.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Text(stringResource(R.string.notification_listener_permission_title), style = MaterialTheme.typography.headlineSmall)
         Text(stringResource(R.string.notification_listener_permission_description))

         if (!onboardingState.anyWatchPaired) {
            Text(stringResource(R.string.notification_listener_permission_disabled_explanation))
         }

         if (onboardingState.hasNotificationListenerPermission) {
            Text("✅")
         } else {
            Button(
               onClick = {
                  requestPermission()
               },
               enabled = onboardingState.anyWatchPaired,
            ) { Text(stringResource(R.string.grant)) }
         }
      }
   }
}

@Composable
private fun CalendarPermission() {
   var rejectedPermission by remember { mutableStateOf(false) }
   val permissionState = rememberMultiplePermissionsState(
      listOf(
         Manifest.permission.READ_CALENDAR,
         Manifest.permission.WRITE_CALENDAR,
      )
   ) { permissions ->
      if (!permissions.values.all { granted -> granted }) {
         rejectedPermission = true
      }
   }

   Card(Modifier.fillMaxWidth()) {
      Column(
         Modifier.padding(8.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Text(stringResource(R.string.calendar_permission_title), style = MaterialTheme.typography.headlineSmall)
         Text(stringResource(R.string.calendar_permission_description))

         MultiPermissionButton(permissionState, rejectedPermission)
      }
   }
}

@Composable
private fun VoicePermission() {
   var rejectedPermission by remember { mutableStateOf(false) }
   val permissionState = rememberMultiplePermissionsState(
      listOf(
         Manifest.permission.RECORD_AUDIO,
      )
   ) { permissions ->
      if (!permissions.values.all { granted -> granted }) {
         rejectedPermission = true
      }
   }

   Card(Modifier.fillMaxWidth()) {
      Column(
         Modifier.padding(8.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Text(stringResource(R.string.voice_permission_title), style = MaterialTheme.typography.headlineSmall)
         Text(stringResource(R.string.voice_permission_description))

         MultiPermissionButton(permissionState, rejectedPermission)
      }
   }
}

@Composable
private fun SinglePermissionButton(
   permissionState: PermissionState,
   rejectedPermission: Boolean,
) {
   val context = LocalContext.current

   if (permissionState.status == PermissionStatus.Granted) {
      Text("✅")
   } else if (rejectedPermission) {
      Button(
         onClick = {
            openSystemPermissionSettings(context)
         }
      ) { Text(stringResource(R.string.open_settings)) }
   } else {
      Button(onClick = { permissionState.launchPermissionRequest() }) { Text(stringResource(R.string.grant)) }
   }
}

@Composable
private fun MultiPermissionButton(
   permissionState: MultiplePermissionsState,
   rejectedPermission: Boolean,
) {
   val context = LocalContext.current
   if (permissionState.allPermissionsGranted) {
      Text("✅")
   } else if (rejectedPermission) {
      Button(
         onClick = {
            openSystemPermissionSettings(context)
         }
      ) { Text(stringResource(R.string.open_settings)) }
   } else {
      Button(onClick = { permissionState.launchMultiplePermissionRequest() }) { Text(stringResource(R.string.grant)) }
   }
}

private fun openSystemPermissionSettings(context: Context) {
   context.startActivity(
      Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
         .setData(
            Uri.fromParts("package", context.getPackageName(), null)
         )
   )
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "test")
internal fun OnboardingContentWithWatchPairedPreview() {
   PreviewTheme {
      OnboardingContent(OnboardingState(anyWatchPaired = true, hasNotificationListenerPermission = false), {}, {})
   }
}

@Preview(heightDp = 1200)
@Composable
private fun OnboardingWholeListPreview() {
   PreviewTheme {
      Column(
         Modifier.padding(8.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         OnboardingScrollContent(OnboardingState(anyWatchPaired = true, hasNotificationListenerPermission = false), {})
      }
   }
}
