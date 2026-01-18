package com.matejdro.micropebble.appstore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.home.AppstoreCollection
import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import com.matejdro.micropebble.appstore.ui.common.WatchAppDisplay
import com.matejdro.micropebble.appstore.ui.common.appGridCells
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import java.net.URI
import kotlin.time.Duration.Companion.milliseconds

private const val categoryHeaderContentType = "categoryHeaderContentType"
private const val appTileContentType = "appTileContentType"

@Inject
@InjectNavigationScreen
class AppstoreScreen(
   private val viewModel: AppstoreViewModel,
   private val navigator: Navigator,
) : Screen<AppstoreScreenKey>() {
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   override fun Content(key: AppstoreScreenKey) {
      val coroutineScope = rememberCoroutineScope()
      remember { coroutineScope.launch { viewModel.ensureAppstoreSource() } }

      var searchExpanded by rememberSaveable { mutableStateOf(false) }
      val getSelectedTab = { viewModel.selectedTab }
      val setSelectedTab: (ApplicationType) -> Unit = { viewModel.selectedTab = it }
      val searchResultState = viewModel.searchResultState.collectAsStateWithLifecycleAndBlinkingPrevention().value

      val appstoreSources by viewModel.appstoreSources.collectAsState(emptyList(), coroutineScope.coroutineContext)

      val canSearch by remember { derivedStateOf { viewModel.appstoreSource?.algoliaData != null } }

      LaunchedEffect(viewModel.selectedTab, viewModel.appstoreSource) {
         viewModel.loadHomePage()
      }
      LaunchedEffect(viewModel.searchQuery, viewModel.selectedTab, viewModel.appstoreSource, searchExpanded) {
         delay(100.milliseconds)
         if (searchExpanded) {
            viewModel.loadSearchResults()
         }
      }

      Scaffold { contentPadding ->
         Column {
            SecondaryTabRow(selectedTabIndex = getSelectedTab().ordinal, modifier = Modifier.padding(contentPadding)) {
               LeadingIconTab(
                  getSelectedTab() == ApplicationType.Watchface,
                  onClick = { setSelectedTab(ApplicationType.Watchface) },
                  text = {
                     Text(stringResource(R.string.watchface))
                  },
                  icon = {
                     Icon(painterResource(R.drawable.ic_watchfaces), contentDescription = null)
                  }
               )

               LeadingIconTab(
                  getSelectedTab() == ApplicationType.Watchapp,
                  onClick = { setSelectedTab(ApplicationType.Watchapp) },
                  text = {
                     Text(stringResource(R.string.watchapp))
                  },
                  icon = {
                     Icon(painterResource(R.drawable.ic_apps), contentDescription = null)
                  }
               )

               var expanded by remember { mutableStateOf(false) }
               ExposedDropdownMenuBox(expanded, onExpandedChange = { expanded = it }) {
                  TextField(
                     value = viewModel.appstoreSource?.name.toString(),
                     onValueChange = {},
                     modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                     readOnly = true,
                     singleLine = true,
                     leadingIcon = { Icon(painterResource(R.drawable.ic_appstore_source), contentDescription = null) },
                     colors = ExposedDropdownMenuDefaults.textFieldColors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                     ),
                  )
                  ExposedDropdownMenu(
                     expanded = expanded,
                     onDismissRequest = { expanded = false }
                  ) {
                     for (source in appstoreSources) {
                        DropdownMenuItem(
                           text = { Text(source.name, style = MaterialTheme.typography.bodyLarge) },
                           onClick = {
                              viewModel.appstoreSource = source
                           },
                           contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                     }
                  }
               }
            }
            if (canSearch) {
               SearchBar(
                  inputField = {
                     SearchBarDefaults.InputField(
                        query = viewModel.searchQuery,
                        onQueryChange = { viewModel.searchQuery = it },
                        onSearch = {},
                        expanded = searchExpanded,
                        onExpandedChange = { searchExpanded = it },
                        placeholder = { Text(stringResource(R.string.search)) },
                        leadingIcon = {
                           Icon(painterResource(R.drawable.ic_search), contentDescription = null)
                        }
                     )
                  },
                  expanded = searchExpanded,
                  onExpandedChange = { searchExpanded = it },
                  modifier = Modifier.align(Alignment.CenterHorizontally),
                  windowInsets = WindowInsets(),
               ) {
                  val outcome = when (searchResultState) {
                     is Outcome.Error -> searchResultState
                     is Outcome.Progress if searchResultState.data != null -> Outcome.Success(searchResultState.data!!)
                     else -> searchResultState
                  }
                  ProgressErrorSuccessScaffold(outcome) { results ->
                     LazyVerticalGrid(
                        columns = appGridCells,
                        modifier = Modifier
                           .fillMaxSize()
                           .padding(8.dp)
                           .clip(CardDefaults.shape),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                     ) {
                        items(results) {
                           WatchAppDisplay(
                              it.toApplication(),
                              navigator,
                              appstoreSource = viewModel.appstoreSource,
                              onlyPartialData = true
                           )
                        }
                     }
                  }
               }
            }

            AppstoreHomepage(
               navigator = navigator,
               state = viewModel.homePageState.collectAsStateWithLifecycleAndBlinkingPrevention().value,
               onRefresh = { viewModel.reloadHomePage() },
               navigateToCollection = { navigator.navigateTo(viewModel.screenKeyFor(it)) },
               appstoreSource = viewModel.appstoreSource,
            )
         }
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppstoreHomepage(
   navigator: Navigator?,
   state: Outcome<AppstoreHomePage>?,
   onRefresh: () -> Unit,
   navigateToCollection: (AppstoreCollection) -> Unit,
   appstoreSource: AppstoreSource? = null,
) {
   PullToRefreshBox(
      isRefreshing = state is Outcome.Progress,
      onRefresh = onRefresh,
      modifier = Modifier.fillMaxWidth(),
   ) {
      if (state is Outcome.Success) {
         val data = state.data
         LazyVerticalGrid(
            columns = appGridCells,
            modifier = Modifier
               .fillMaxSize()
               .padding(horizontal = 8.dp)
               .padding(top = 8.dp)
               .clip(CardDefaults.shape),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
         ) {
            for (collection in data.collections) {
               item(collection.slug, contentType = categoryHeaderContentType, span = { GridItemSpan(maxLineSpan) }) {
                  Box(
                     modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
                  ) {
                     TextButton(
                        onClick = { navigateToCollection(collection) },
                        modifier = Modifier
                           .fillMaxWidth()
                           .padding(8.dp)
                     ) {
                        Row(
                           modifier = Modifier.fillMaxSize(),
                           horizontalArrangement = Arrangement.SpaceBetween,
                           verticalAlignment = Alignment.CenterVertically
                        ) {
                           Text(collection.name, Modifier.padding(8.dp), style = MaterialTheme.typography.titleLarge)
                           Row(
                              horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
                           ) {
                              Text(stringResource(R.string.seeAll))
                              Icon(
                                 painter = painterResource(R.drawable.ic_chevron_forward),
                                 contentDescription = null,
                                 modifier = Modifier.padding(8.dp)
                              )
                           }
                        }
                     }
                  }
               }
               for (appId in collection.appIds) {
                  item(contentType = appTileContentType) {
                     val app = data.applicationsById[appId]
                     if (app == null) {
                        Text(
                           "App with ID $appId not found", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error
                        )
                     } else {
                        WatchAppDisplay(app, navigator, appstoreSource = appstoreSource)
                     }
                  }
               }
            }
         }
      }
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun AppstoreHomepagePreview() {
   PreviewTheme {
      val string = URI("https://appstore-api.rebble.io/api/v1/home/apps?platform=all").toURL()
         .readText() // val string = Json.encodeToString(Json.parseToJsonElement(rawString).jsonObject["data"]?.jsonArray[0])
      AppstoreHomepage(
         navigator = null,
         state = Outcome.Success(Json.decodeFromString(string)),
         onRefresh = {},
         navigateToCollection = {},
      )
   }
}
