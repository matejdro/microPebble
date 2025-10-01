package com.matejdro.micropebble.apps.ui.list

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import io.rebble.libpebblecommon.locker.AppProperties
import io.rebble.libpebblecommon.locker.AppType
import io.rebble.libpebblecommon.locker.LockerWrapper
import io.rebble.libpebblecommon.locker.SystemApps
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
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
            deleteApp = viewModel::deleteApp,
            setOrder = viewModel::reorderApp
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
   setOrder: (Uuid, Int) -> Unit,
) {
   ErrorAlertDialog(actionStatus, errorText = { it.installUserFriendlyErrorMessage() })
   var selectedTab by remember { mutableIntStateOf(0) }

   val reorderState = rememberReorderState<LockerWrapper>(dragAfterLongPress = true)

   ReorderContainer(
      reorderState,
      enabled = selectedTab == 1
   ) {
      val displayedItems = if (selectedTab == 0) state.watchfaces else state.watchapps
      var reorderingList by remember(displayedItems) { mutableStateOf(displayedItems) }

      LazyColumn(
         Modifier.fillMaxSize(),
         contentPadding = WindowInsets.safeDrawing.asPaddingValues()
      ) {
         item("ButtonsBar") {
            if (actionStatus is Outcome.Progress) {
               CircularProgressIndicator(Modifier.padding(8.dp))
            } else {
               Button(onClick = installFromPbw, Modifier.padding(8.dp)) {
                  Text("Install from PBW")
               }
            }
         }

         item("Divider") { HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface) }

         item("TabBar") {
            TabRow(selectedTabIndex = selectedTab) {
               Tab(selectedTab == 0, onClick = { selectedTab = 0 }, modifier = Modifier.sizeIn(minHeight = 48.dp)) {
                  Text("Watchfaces")
               }

               Tab(selectedTab == 1, onClick = { selectedTab = 1 }, modifier = Modifier.sizeIn(minHeight = 48.dp)) {
                  Text("Watchapps")
               }
            }
         }

         itemsWithDivider(
            reorderingList,
            key = { it.properties.id.toString() },
            contentType = { "app" },
            modifier = { Modifier.animateItem() }) { app ->
            ReorderableItem(
               state = reorderState,
               key = app.properties.id,
               data = app,
               onDragEnter = { state ->
                  reorderingList = reorderingList.toMutableList().apply {
                     val index = indexOf(app)
                     if (index == -1) return@ReorderableItem
                     remove(state.data)
                     add(index, state.data)

                     // scope.launch {
                     //    handleLazyListScroll(
                     //       lazyListState = lazyListState,
                     //       dropIndex = index,
                     //    )
                     // }
                  }
               },
               onDrop = {
                  setOrder(it.data.properties.id, reorderingList.indexOf(it.data))
               },
               draggableContent = {
                  App(
                     app,
                     actionStatus !is Outcome.Progress,
                     delete = { deleteApp(app.properties.id) },
                  )
               },
               modifier = Modifier
                  .fillMaxWidth()
            ) {
               App(
                  app,
                  actionStatus !is Outcome.Progress,
                  delete = { deleteApp(app.properties.id) },
                  Modifier.graphicsLayer {
                     alpha = if (isDragging) 0f else 1f
                  }
               )
            }
         }
      }
   }
}

@Composable
private fun App(app: LockerWrapper, enableActions: Boolean, delete: () -> Unit, modifier: Modifier = Modifier) {
   Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
         .background(MaterialTheme.colorScheme.surface)
         .padding(8.dp)
         .sizeIn(minHeight = 48.dp)
   ) {
      Column(
         modifier = Modifier
            .weight(1f),
         verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Text(app.properties.title, style = MaterialTheme.typography.bodyMedium)
      }

      if (app !is LockerWrapper.SystemApp) {
         OutlinedButton(onClick = delete, enabled = enableActions) {
            Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(R.string.delete_app))
         }
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun WatchappListScreenContentPreview() {
   PreviewTheme {
      val state = WatchappListState(fakeApps, fakeApps)

      WatchappListScreenContent(
         state,
         Outcome.Success(Unit),
         {},
         {},
         { _, _ -> }
      )
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun WatchappListInstallingPreview() {
   PreviewTheme {
      val state = WatchappListState(fakeApps, fakeApps)

      WatchappListScreenContent(
         state,
         Outcome.Progress(Unit),
         {},
         {},
         { _, _ -> }
      )
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun WatchappListInstallingErrorPreview() {
   PreviewTheme {
      val state = WatchappListState(fakeApps, fakeApps)

      WatchappListScreenContent(
         state,
         Outcome.Error(NoNetworkException()),
         {},
         {},
         { _, _ -> }
      )
   }
}

private val fakeApps = List(10) {
   if (it % 2 == 0) {
      LockerWrapper.NormalApp(
         AppProperties(
            Uuid.fromLongs(0L, it.toLong()),
            AppType.Watchapp,
            "App $it",
            "Dev $it",
            emptyList(),
            null,
            null,
            null,
            null,
            null,
            it
         ),
         true,
         false,
         false
      )
   } else {
      LockerWrapper.SystemApp(
         AppProperties(
            Uuid.fromLongs(0L, it.toLong()),
            if (it % 4 == 0) AppType.Watchapp else AppType.Watchface,
            "System App $it",
            "Dev $it",
            emptyList(),
            null,
            null,
            null,
            null,
            null,
            it
         ),
         SystemApps.entries.first()
      )
   }
}
