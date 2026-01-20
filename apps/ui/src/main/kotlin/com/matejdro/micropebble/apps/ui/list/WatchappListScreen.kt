package com.matejdro.micropebble.apps.ui.list

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.apps.ui.R
import com.matejdro.micropebble.apps.ui.errors.installUserFriendlyErrorMessage
import com.matejdro.micropebble.apps.ui.webviewconfig.AppConfigScreenKey
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.common.util.parseVersionString
import com.matejdro.micropebble.common.util.toVersionString
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import com.matejdro.micropebble.navigation.keys.HomeScreenKey
import com.matejdro.micropebble.navigation.keys.WatchappListKey
import com.matejdro.micropebble.ui.components.ErrorAlertDialog
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import com.matejdro.micropebble.ui.lists.ReorderableListContainer
import io.rebble.libpebblecommon.locker.AppProperties
import io.rebble.libpebblecommon.locker.AppType
import io.rebble.libpebblecommon.locker.LockerWrapper
import io.rebble.libpebblecommon.locker.SystemApps
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.instructions.replaceTopWith
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import kotlin.uuid.Uuid
import com.matejdro.micropebble.sharedresources.R as sharedR

@InjectNavigationScreen
@ContributesScreenBinding
class WatchappListScreen(
   private val viewModel: WatchappListViewModel,
   private val navigator: Navigator,
) : Screen<WatchappListKey>() {
   @Composable
   @OptIn(ExperimentalMaterial3ExpressiveApi::class)
   override fun Content(key: WatchappListKey) {
      val state = viewModel.uiState.collectAsStateWithLifecycleAndBlinkingPrevention().value
      val appSources = viewModel.appInstallSourceStatus.collectAsState(null).value?.data
      val appStatuses =
         viewModel.updatableWatchapps.collectAsState(null).value ?: Outcome.Progress()
      val appInstallSources =
         viewModel.appInstallSourceStatus.collectAsState(null).value ?: Outcome.Progress()
      val sources by viewModel.appstoreSources.collectAsState(emptyList())
      val appUpdatingStatus = viewModel.appUpdatingStatus.collectAsState().value
      val selectPbwResult = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { pbwUri ->
         if (pbwUri != null) {
            viewModel.startInstall(pbwUri)
         }
      }

      var fixMissingSourceAppId: Uuid? by remember { mutableStateOf(null) }

      ProgressErrorSuccessScaffold(
         state,
         Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
      ) {
         WatchappListScreenContent(
            it,
            appStatuses,
            appUpdatingStatus,
            viewModel.actionStatus.collectAsStateWithLifecycleAndBlinkingPrevention().value,
            installFromPbw = {
               selectPbwResult.launch(arrayOf("*/*"))
            },
            installFromAppstore = {
               navigator.navigateTo(AppstoreScreenKey)
            },
            deleteApp = viewModel::deleteApp,
            updateApp = viewModel::updateApp,
            setOrder = viewModel::reorderApp,
            openConfiguration = { appUuid ->
               navigator.navigateTo(AppConfigScreenKey(appUuid))
            },
            fixMissingSource = { appId -> fixMissingSourceAppId = appId }
         )
      }

      PbwInstallDialog(key)
      if (fixMissingSourceAppId != null) {
         FixMissingSourceDialog(
            sources,
            startingSource = { appInstallSources.data?.get(fixMissingSourceAppId)?.sourceId },
            onSubmitted = { sourceId ->
               fixMissingSourceAppId?.let { appId ->
                  appSources?.get(appId)?.let { source ->
                     viewModel.changeAppInstallSource(source, sourceId)
                  }
               }
               fixMissingSourceAppId = null
            },
            onCanceled = {
               fixMissingSourceAppId = null
            },
         )
      }
   }

   @Composable
   private fun PbwInstallDialog(key: WatchappListKey) {
      val pbwFile = key.pbwFile
      if (pbwFile != null) {
         fun closeDialog() {
            navigator.replaceTopWith(HomeScreenKey(key.copy(pbwFile = null)))
         }

         AlertDialog(onDismissRequest = ::closeDialog, confirmButton = {
            TextButton(onClick = {
               closeDialog()
               viewModel.startInstall(pbwFile.uri)
            }) { Text(stringResource(sharedR.string.ok)) }
         }, dismissButton = {
            TextButton(onClick = { closeDialog() }) { Text(stringResource(sharedR.string.cancel)) }
         }, title = {
            Text(stringResource(R.string.install_from_pbw))
         }, text = {
            Text(stringResource(R.string.install_confirmation, pbwFile.filename))
         })
      }
   }

   @Composable
   private fun FixMissingSourceDialog(
      sources: List<AppstoreSource>,
      startingSource: () -> Uuid?,
      onSubmitted: (sourceId: Uuid) -> Unit,
      onCanceled: () -> Unit,
   ) {
      var id: Uuid? by remember { mutableStateOf(startingSource()) }
      AlertDialog(
         onDismissRequest = onCanceled,
         title = {
            Text(stringResource(R.string.change_source))
         },
         text = {
            Column(Modifier.selectableGroup()) {
               for (source in sources) {
                  Row(
                     Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(id == source.id, onClick = { id = source.id }),
                     verticalAlignment = Alignment.CenterVertically,
                  ) {
                     RadioButton(selected = id == source.id, onClick = null)
                     Text(source.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
                  }
               }
            }
         },
         confirmButton = {
            TextButton(
               onClick = { id?.let { onSubmitted(it) } },
               enabled = id != null,
            ) {
               Text(stringResource(sharedR.string.ok))
            }
         },
         dismissButton = {
            TextButton(onClick = onCanceled) {
               Text(stringResource(sharedR.string.cancel))
            }
         }
      )
   }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WatchappListScreenContent(
   state: WatchappListState,
   appStatuses: Outcome<Map<Uuid, AppStatus>>,
   appUpdatingStatus: Map<Uuid, Outcome<Unit>>,
   actionStatus: Outcome<Unit>?,
   installFromPbw: () -> Unit,
   installFromAppstore: () -> Unit,
   deleteApp: (Uuid) -> Unit,
   updateApp: (Uuid) -> Unit,
   fixMissingSource: (Uuid) -> Unit,
   setOrder: (Uuid, Int) -> Unit,
   openConfiguration: (Uuid) -> Unit,
) {
   Box {
      ErrorAlertDialog(actionStatus, errorText = { it.installUserFriendlyErrorMessage() })
      var selectedTab by rememberSaveable { mutableIntStateOf(0) }
      val listState = rememberLazyListState()

      ReorderableListContainer(
         if (selectedTab == 0) state.watchfaces else state.watchapps, listState, enabled = selectedTab == 1
      ) { displayedItems ->
         LazyColumn(
            state = listState, modifier = Modifier.fillMaxSize(), contentPadding = WindowInsets.safeDrawing.asPaddingValues()
         ) {
            item("TabBar") {
               SecondaryTabRow(selectedTabIndex = selectedTab) {
                  LeadingIconTab(selectedTab == 0, onClick = { selectedTab = 0 }, text = {
                     Text(stringResource(R.string.watchfaces))
                  }, icon = {
                     Icon(painterResource(R.drawable.ic_watchfaces), contentDescription = null)
                  })
                  LeadingIconTab(selectedTab == 1, onClick = { selectedTab = 1 }, text = {
                     Text(stringResource(R.string.watchapps))
                  }, icon = {
                     Icon(painterResource(R.drawable.ic_apps), contentDescription = null)
                  })
               }
            }

            if (displayedItems.isEmpty()) {
               item {
                  Text(
                     "None installed yet",
                     Modifier
                        .padding(32.dp)
                        .fillMaxWidth()
                        .wrapContentSize(),
                     style = MaterialTheme.typography.titleSmall
                  )
               }
            }

            itemsWithDivider(
               displayedItems,
               key = { it.properties.id.toString() },
               contentType = { "app" },
               modifier = { Modifier.animateItem() }
            ) { app ->
               ReorderableListItem(
                  key = app.properties.id, data = app, setOrder = {
                     setOrder(app.properties.id, it)
                  }, modifier = Modifier.fillMaxWidth()
               ) { modifier ->
                  val id = app.properties.id
                  // If the app has an updating status, then use it. Otherwise, use the normal app status.
                  val appStatus = appUpdatingStatus[id]?.let {
                     when (it) {
                        is Outcome.Success -> Outcome.Success(AppStatus.JustUpdated)
                        is Outcome.Progress -> Outcome.Progress(AppStatus.Updating)
                        is Outcome.Error -> Outcome.Success(AppStatus.UpdateFailed(it.exception))
                     }
                  } ?: appStatuses.mapData { it[id] ?: AppStatus.NotUpdatable }
                  App(
                     app,
                     actionStatus !is Outcome.Progress,
                     delete = { deleteApp(id) },
                     appStatus = appStatus,
                     update = { updateApp(id) },
                     changeAppSource = { fixMissingSource(id) },
                     openConfiguration = { openConfiguration(id) },
                     modifier
                  )
               }
            }
         }
      }

      var expanded by remember { mutableStateOf(false) }

      FloatingActionButtonMenu(
         expanded, button = {
            ToggleFloatingActionButton(expanded, onCheckedChange = { expanded = it }) {
               val close = painterResource(R.drawable.ic_close) as VectorPainter
               val open = painterResource(R.drawable.ic_open) as VectorPainter
               val imageVector by remember {
                  derivedStateOf {
                     @Suppress("MagicNumber")
                     if (checkedProgress > 0.5f) close else open
                  }
               }
               Icon(
                  painter = imageVector,
                  contentDescription = null,
                  modifier = Modifier.animateIcon({ checkedProgress }),
               )
            }
         },
         modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .align(Alignment.BottomEnd)
      ) {
         FloatingActionButtonMenuItem(
            onClick = installFromPbw,
            text = { Text(stringResource(R.string.install_from_pbw)) },
            icon = { Icon(painterResource(R.drawable.ic_install_pbw), contentDescription = null) },
         )
         FloatingActionButtonMenuItem(
            onClick = installFromAppstore,
            text = { Text(stringResource(R.string.install_from_store)) },
            icon = { Icon(painterResource(R.drawable.ic_appstore), contentDescription = null) },
         )
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun App(
   app: LockerWrapper,
   enableActions: Boolean,
   delete: () -> Unit,
   appStatus: Outcome<AppStatus>,
   update: () -> Unit,
   changeAppSource: () -> Unit,
   openConfiguration: () -> Unit,
   modifier: Modifier = Modifier,
) {
   Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = modifier
         .background(MaterialTheme.colorScheme.surface)
         .padding(8.dp)
         .sizeIn(minHeight = 48.dp)
   ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
         Text(app.properties.title, style = MaterialTheme.typography.bodyMedium)
      }

      if (appStatus is Outcome.Success) {
         when (val status = appStatus.data) {
            is AppStatus.UpdateFailed -> UpdateFailedIndicator()
            is AppStatus.Updatable -> UpdateButton(update, status)
            AppStatus.JustUpdated -> UpdateInstalledIndicator()
            AppStatus.NotUpdatable -> {}
            else -> AppStatusFailedIndicator(status)
         }
      } else {
         AppStatusIndicator(appStatus)
      }

      ChangeSourceButton(appStatus, changeAppSource) {
         Icon(painterResource(R.drawable.ic_appstore_source), contentDescription = stringResource(R.string.fix_missing_source))
      }

      if (app is LockerWrapper.NormalApp && app.configurable) {
         OutlinedButton(onClick = openConfiguration, contentPadding = PaddingValues(8.dp)) {
            Icon(painterResource(R.drawable.ic_settings), contentDescription = stringResource(R.string.configure))
         }
      }

      if (app !is LockerWrapper.SystemApp) {
         OutlinedButton(onClick = delete, enabled = enableActions, contentPadding = PaddingValues(8.dp)) {
            Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(R.string.delete_app))
         }
      }
   }
}

@Composable
private fun ChangeSourceButton(
   appStatus: Outcome<AppStatus>,
   changeAppSource: () -> Unit,
   content: @Composable (RowScope.() -> Unit),
) {
   if (appStatus.data == AppStatus.MissingSource) {
      Button(
         onClick = changeAppSource,
         contentPadding = PaddingValues(8.dp),
         colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
         ),
         content = content
      )
   } else if (appStatus is Outcome.Success && appStatus.data != AppStatus.NotUpdatable) {
      OutlinedButton(onClick = changeAppSource, contentPadding = PaddingValues(8.dp), content = content)
   }
}

@Composable
private fun UpdateInstalledIndicator() {
   Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
      Icon(
         painterResource(R.drawable.ic_download_done),
         contentDescription = stringResource(R.string.not_updatable),
         tint = MaterialTheme.colorScheme.tertiary
      )
   }
}

@Composable
private fun UpdateFailedIndicator() {
   Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
      Icon(
         painterResource(R.drawable.ic_error),
         contentDescription = stringResource(R.string.not_updatable),
         tint = MaterialTheme.colorScheme.error
      )
   }
}

@Composable
private fun UpdateButton(update: () -> Unit, status: AppStatus.Updatable) {
   Button(onClick = update, contentPadding = PaddingValues(8.dp)) {
      Icon(painterResource(R.drawable.ic_update), contentDescription = stringResource(R.string.update))
      Text(status.fromVersion.toVersionString(), modifier = Modifier.padding(start = 8.dp))
      Icon(painterResource(R.drawable.ic_chevron_forward), contentDescription = stringResource(R.string.update))
      Text(
         status.toVersion.toVersionString(),
         style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
         modifier = Modifier.padding(end = 8.dp)
      )
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppStatusFailedIndicator(status: AppStatus) {
   val string = stringResource(
      if (status == AppStatus.AppNotFound) {
         R.string.not_updatable_app_not_found_tooltip
      } else {
         R.string.not_updatable_tooltip
      }
   )
   TooltipBox(
      TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Left),
      tooltip = { PlainTooltip { Text(string) } },
      state = rememberTooltipState(),
   ) {
      OutlinedButton(onClick = {}, contentPadding = PaddingValues(8.dp), enabled = false) {
         Icon(painterResource(R.drawable.ic_cloud_off), contentDescription = stringResource(R.string.not_updatable))
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun AppStatusIndicator(appStatus: Outcome<AppStatus>) {
   Box(Modifier.padding(horizontal = 8.dp)) {
      if (appStatus.data == AppStatus.Updating) {
         val stroke = WavyProgressIndicatorDefaults.circularIndicatorStroke.apply {
            Stroke(
               width = width / 2.0f,
               miter = miter,
               cap = cap,
               join = join,
               pathEffect = pathEffect
            )
         }
         val trackStroke = WavyProgressIndicatorDefaults.circularTrackStroke.apply {
            Stroke(
               width = width / 2.0f,
               miter = miter,
               cap = cap,
               join = join,
               pathEffect = pathEffect
            )
         }
         CircularWavyProgressIndicator(stroke = stroke, trackStroke = trackStroke)
      } else {
         CircularProgressIndicator(strokeWidth = 2.dp)
      }
      Icon(
         if (appStatus.data == AppStatus.Updating) {
            painterResource(R.drawable.ic_download)
         } else {
            painterResource(R.drawable.ic_update)
         },
         contentDescription = stringResource(R.string.update),
         modifier = Modifier.align(Alignment.Center),
         tint = ProgressIndicatorDefaults.circularColor
      )
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
         Outcome.Success(
            mapOf(
               fakeApps[2].properties.id to AppStatus.Updatable(
                  parseVersionString("1.0")!!,
                  parseVersionString("1.2")!!
               ),
               fakeApps[4].properties.id to AppStatus.Updatable(
                  parseVersionString("1.0")!!,
                  parseVersionString("1.2")!!
               ),
               fakeApps[6].properties.id to AppStatus.Updatable(
                  parseVersionString("1.0")!!,
                  parseVersionString("1.2")!!
               ),
               fakeApps[8].properties.id to AppStatus.Error
            )
         ),
         mapOf(
            fakeApps[2].properties.id to Outcome.Progress(Unit),
            fakeApps[4].properties.id to Outcome.Success(Unit),
            fakeApps[6].properties.id to Outcome.Error(UnknownCauseException()),
         ),
         Outcome.Success(Unit),
         {},
         {},
         {},
         {},
         {},
         { _, _ -> },
      ) {}
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
         Outcome.Success(emptyMap()),
         emptyMap(),
         Outcome.Progress(Unit),
         {},
         {},
         {},
         {},
         {},
         { _, _ -> }
      ) {}
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
         Outcome.Success(emptyMap()),
         emptyMap(),
         Outcome.Error(NoNetworkException()),
         {},
         {},
         {},
         {},
         {},
         { _, _ -> }
      ) {}
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun WatchappListScreenContentEmptyPreview() {
   PreviewTheme {
      val state = WatchappListState(emptyList(), emptyList())

      WatchappListScreenContent(
         state,
         Outcome.Success(emptyMap()),
         emptyMap(),
         Outcome.Success(Unit),
         {},
         {},
         {},
         {},
         {},
         { _, _ -> }
      ) {}
   }
}

private val fakeApps = List(10) {
   if (it % 2 == 0) {
      LockerWrapper.NormalApp(
         AppProperties(
            Uuid.fromLongs(0L, it.toLong()), AppType.Watchapp, "App $it", "Dev $it", emptyList(), null, null, null, null, null, it
         ),
         true,
         it % 4 == 0,
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
