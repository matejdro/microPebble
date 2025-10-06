package com.matejdro.micropebble.apps.ui.developerconnection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.apps.ui.R
import com.matejdro.micropebble.navigation.keys.DeveloperConnectionScreenKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class DeveloperConnectionScreen(
   private val viewModel: DeveloperConnectionScreenViewModel,
) : Screen<DeveloperConnectionScreenKey>() {
   @Composable
   override fun Content(key: DeveloperConnectionScreenKey) {
      val state = viewModel.uiState.collectAsStateWithLifecycleAndBlinkingPrevention().value
      ProgressErrorSuccessScaffold(
         state,
         Modifier
            .fillMaxSize()
            .safeDrawingPadding()
      ) { successState ->
         DeveloperConnectionScreenContent(successState, viewModel::selectDevice, {
            if (it) viewModel.startDevConn() else viewModel.stopDevConn()
         })
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeveloperConnectionScreenContent(
   state: DeveloperConnectionScreenState,
   selectWatch: (String) -> Unit,
   toggleDeveloperConnection: (Boolean) -> Unit,
) {
   Column(
      Modifier
         .safeDrawingPadding()
         .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
   ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
         Text(text = stringResource(R.string.active_watch))
         var isDropdownExpanded by remember { mutableStateOf(false) }

         ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { if (state.availableWatches.isNotEmpty()) isDropdownExpanded = it },
            modifier = Modifier
               .weight(1f)
               .wrapContentWidth(Alignment.End)
         ) {
            val selectedWatchName = state.availableWatches.firstOrNull { it.serial == state.selectedWatchSerial }?.title
               ?: stringResource(R.string.no_connected_watch)
            DropdownMenuItem(
               onClick = { isDropdownExpanded = !isDropdownExpanded },
               text = {
                  Text(selectedWatchName)
               },
               trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isDropdownExpanded) },
               enabled = state.availableWatches.isNotEmpty(),
            )

            ExposedDropdownMenu(isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
               for (watch in state.availableWatches) {
                  DropdownMenuItem(
                     onClick = {
                        selectWatch(watch.serial)
                        isDropdownExpanded = false
                     },
                     text = {
                        Text(watch.title)
                     }
                  )
               }
            }
         }
      }

      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
         Text(text = stringResource(R.string.enable_developer_connection))

         Switch(
            state.connectionActive,
            onCheckedChange = { toggleDeveloperConnection(it) },
            Modifier
               .weight(1f)
               .wrapContentWidth(Alignment.End),
            enabled = state.selectedWatchSerial.isNotEmpty()
         )
      }

      if (state.connectionActive) {
         Column {
            Text(text = stringResource(R.string.ip))
            Text(state.ip)
         }
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "test")
internal fun DeveloperConnectionScreenPreview() {
   PreviewTheme {
      DeveloperConnectionScreenContent(
         DeveloperConnectionScreenState(
            listOf(
               DeveloperConnectionScreenState.Watch("Pebble Time", "00"),
            ),
            "00",
            true,
            "127.0.0.1"
         ),
         {},
         {}
      )
   }
}

@Preview(showBackground = true)
@Composable
@ShowkaseComposable(group = "test")
internal fun DeveloperConnectionEmptyScreenPreview() {
   PreviewTheme {
      DeveloperConnectionScreenContent(
         DeveloperConnectionScreenState(
            emptyList(),
            "",
            false,
            ""
         ),
         {},
         {}
      )
   }
}
