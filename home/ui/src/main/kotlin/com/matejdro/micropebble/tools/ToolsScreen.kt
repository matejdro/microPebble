package com.matejdro.micropebble.tools

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.home.ui.R
import com.matejdro.micropebble.navigation.keys.CalendarListScreenKey
import com.matejdro.micropebble.navigation.keys.DeveloperConnectionScreenKey
import com.matejdro.micropebble.navigation.keys.OnboardingKey
import com.matejdro.micropebble.ui.components.ErrorAlertDialog
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
@ContributesScreenBinding
class ToolsScreen(
   private val navigator: Navigator,
   private val viewModel: ToolsViewModel,
) : Screen<ToolsScreenKey>() {
   @Composable
   override fun Content(key: ToolsScreenKey) {
      val appVersion = viewModel.appVersion.collectAsStateWithLifecycle().value
      val logSaveStatus = viewModel.logSave.collectAsStateWithLifecycleAndBlinkingPrevention().value

      Surface {
         ToolsScreenContent(
            appVersion,
            logSaveStatus,
            { navigator.navigateTo(OnboardingKey) },
            { navigator.navigateTo(DeveloperConnectionScreenKey) },
            { navigator.navigateTo(CalendarListScreenKey) },
            viewModel::getLogs,
            viewModel::resetLog,
         )
      }
   }
}

@Composable
private fun ToolsScreenContent(
   appVersion: String,
   loggingTransmissionState: Outcome<Uri?>?,
   openPermissions: () -> Unit,
   openDevConnection: () -> Unit,
   openCalendarSettings: () -> Unit,
   startLogSaving: () -> Unit,
   notifyLogIntentSent: () -> Unit,
) {
   LazyVerticalGrid(
      GridCells.Adaptive(175.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
      modifier = Modifier
         .padding(horizontal = 16.dp)
         .fillMaxSize()
   ) {
      item {
         ToolButton(openDevConnection, R.drawable.developer_connection, R.string.developer_connection)
      }

      item {
         ToolButton(openPermissions, R.drawable.permissions, R.string.permissions)
      }

      item {
         ErrorAlertDialog(loggingTransmissionState)

         if (loggingTransmissionState is Outcome.Progress<*>) {
            CircularProgressIndicator(
               Modifier
                  .size(60.dp)
                  .wrapContentWidth()
            )
         } else {
            ToolButton(startLogSaving, R.drawable.logs, R.string.save_logs)

            val context = LocalContext.current
            SideEffect {
               loggingTransmissionState?.data?.let { targetUri ->
                  val activityIntent = Intent(Intent.ACTION_SEND)
                  activityIntent.putExtra(Intent.EXTRA_STREAM, targetUri)
                  activityIntent.setType("application/zip")

                  activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  activityIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                  context.startActivity(Intent.createChooser(activityIntent, null))
                  notifyLogIntentSent()
               }
            }
         }
      }

      item {
         ToolButton(openCalendarSettings, R.drawable.calendar_settings, R.string.calendar)
      }

      item(span = { GridItemSpan(maxLineSpan) }) {
         Text(
            stringResource(R.string.version, appVersion),
            Modifier
               .fillMaxWidth()
               .wrapContentWidth(Alignment.CenterHorizontally)
         )
      }
   }
}

@Composable
private fun ToolButton(onClick: () -> Unit, icon: Int, text: Int) {
   Button(onClick = onClick, Modifier.sizeIn(minHeight = 60.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
         Icon(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
         )

         Text(stringResource(text))
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "test")
internal fun ToolsScreenPreview() {
   PreviewTheme {
      ToolsScreenContent(
         appVersion = "1.0.0-alpha07",
         loggingTransmissionState = Outcome.Success(null),
         openPermissions = {},
         openDevConnection = {},
         openCalendarSettings = {},
         startLogSaving = {}
      ) {}
   }
}
