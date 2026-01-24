package com.matejdro.micropebble.appstore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.appstore.api.store.application.AlgoliaApplication
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.home.AppstoreCollection
import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import com.matejdro.micropebble.appstore.ui.common.WatchAppDisplay
import com.matejdro.micropebble.appstore.ui.common.appGridCells
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import com.matejdro.micropebble.navigation.keys.AppstoreSourcesScreenKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import com.matejdro.micropebble.ui.errors.NoSourcesDisplay
import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import java.net.URI
import com.matejdro.micropebble.sharedresources.R as sharedR

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
      val appstoreSources = viewModel.appstoreSources.collectAsStateWithLifecycle(null).value ?: return

      if (appstoreSources.isEmpty()) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NoSourcesDisplay {
               Button(
                  onClick = { navigator.navigateTo(AppstoreSourcesScreenKey) },
                  modifier = Modifier.align(Alignment.CenterHorizontally),
               ) {
                  Text(stringResource(sharedR.string.manage_appstore_sources))
                  Icon(painterResource(R.drawable.ic_chevron_forward), contentDescription = null)
               }
            }
         }
         return
      }

      AppstoreScreenScaffold(
         selectedTab = viewModel.selectedTab,
         setSelectedTab = { viewModel.selectedTab = it },
         source = viewModel.appstoreSource,
         appstoreSources = appstoreSources,
         setSelectedSource = { viewModel.appstoreSource = it },
         searchQuery = viewModel.searchQuery,
         setSearchQuery = { viewModel.searchQuery = it },
         searchResults = viewModel.searchResults.collectAsStateWithLifecycleAndBlinkingPrevention().value,
         navigator = navigator,
         homePageState = viewModel.homePageState.collectAsStateWithLifecycleAndBlinkingPrevention().value,
         onRefresh = viewModel::reloadHomePage,
         navigateToCollection = { navigator.navigateTo(viewModel.screenKeyFor(it)) }
      )
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppstoreScreenScaffold(
   selectedTab: ApplicationType,
   setSelectedTab: (ApplicationType) -> Unit,
   source: AppstoreSource?,
   appstoreSources: List<AppstoreSource>,
   setSelectedSource: (AppstoreSource) -> Unit,
   searchQuery: String,
   setSearchQuery: (String) -> Unit,
   searchResults: Outcome<List<AlgoliaApplication>>?,
   navigator: Navigator?,
   homePageState: Outcome<AppstoreHomePage>?,
   onRefresh: () -> Unit,
   navigateToCollection: (AppstoreCollection) -> Unit,
) {
   Scaffold { contentPadding ->
      Column {
         TypeSelector(
            selectedTab = selectedTab,
            setSelectedTab = setSelectedTab,
            appstoreSources = appstoreSources,
            source = source,
            setSelectedSource = setSelectedSource,
            modifier = Modifier.padding(contentPadding)
         )

         if (source?.algoliaData != null) {
            AppsSearchBox(
               searchQuery = searchQuery,
               setSearchQuery = setSearchQuery,
               searchResults = searchResults,
               navigator = navigator,
               source = source,
               modifier = Modifier.align(Alignment.CenterHorizontally)
            )
         }

         AppstoreHomepage(
            navigator = navigator,
            state = homePageState,
            onRefresh = onRefresh,
            navigateToCollection = navigateToCollection,
            appstoreSource = source,
         )
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ColumnScope.AppsSearchBox(
   searchQuery: String,
   setSearchQuery: (String) -> Unit,
   searchResults: Outcome<List<AlgoliaApplication>>?,
   navigator: Navigator?,
   source: AppstoreSource,
   modifier: Modifier = Modifier,
) {
   var searchExpanded by rememberSaveable { mutableStateOf(false) }
   SearchBar(
      inputField = {
         SearchBarDefaults.InputField(
            query = searchQuery,
            onQueryChange = setSearchQuery,
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
      modifier = modifier,
      windowInsets = WindowInsets(),
   ) {
      val outcome = when (searchResults) {
         is Outcome.Error -> searchResults
         is Outcome.Progress if searchResults.data != null -> Outcome.Success(searchResults.data!!)
         else -> searchResults
      }
      ProgressErrorSuccessScaffold(outcome) { results ->
         LazyVerticalStaggeredGrid(
            columns = appGridCells,
            modifier = Modifier
               .fillMaxSize()
               .padding(8.dp)
               .clip(CardDefaults.shape),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
         ) {
            items(results) {
               WatchAppDisplay(
                  it.toApplication(),
                  navigator,
                  appstoreSource = source,
                  onlyPartialData = true
               )
            }
         }
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TypeSelector(
   selectedTab: ApplicationType,
   setSelectedTab: (ApplicationType) -> Unit,
   appstoreSources: List<AppstoreSource>,
   source: AppstoreSource?,
   setSelectedSource: (AppstoreSource) -> Unit,
   modifier: Modifier = Modifier,
) {
   SecondaryTabRow(selectedTabIndex = selectedTab.ordinal, modifier = modifier) {
      LeadingIconTab(
         selectedTab == ApplicationType.Watchface,
         onClick = { setSelectedTab(ApplicationType.Watchface) },
         text = {
            Text(stringResource(R.string.watchface))
         },
         icon = {
            Icon(painterResource(R.drawable.ic_watchfaces), contentDescription = null)
         }
      )

      LeadingIconTab(
         selectedTab == ApplicationType.Watchapp,
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
            value = source?.name ?: stringResource(R.string.no_source_selected),
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
            for (source in appstoreSources.filter { it.enabled }) {
               DropdownMenuItem(
                  text = { Text(source.name, style = MaterialTheme.typography.bodyLarge) },
                  onClick = { setSelectedSource(source) },
                  contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
               )
            }
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
         LazyVerticalStaggeredGrid(
            columns = appGridCells,
            modifier = Modifier
               .fillMaxSize()
               .padding(horizontal = 8.dp)
               .padding(top = 8.dp)
               .clip(CardDefaults.shape),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
         ) {
            for (collection in data.collections) {
               item(collection.slug, contentType = categoryHeaderContentType, span = StaggeredGridItemSpan.FullLine) {
                  AppstoreHeader(navigateToCollection, collection)
               }
               for (appId in collection.appIds) {
                  item(contentType = appTileContentType) {
                     val app = data.applicationsById[appId]
                     if (app != null) {
                        WatchAppDisplay(app, navigator, appstoreSource = appstoreSource)
                     }
                  }
               }
            }
         }
      }
   }
}

@Composable
private fun AppstoreHeader(navigateToCollection: (AppstoreCollection) -> Unit, collection: AppstoreCollection) {
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

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun AppstoreHomepagePreview() {
   PreviewTheme {
      val string = URI("https://appstore-api.rebble.io/api/v1/home/apps?platform=all").toURL().readText()
      AppstoreScreenScaffold(
         selectedTab = ApplicationType.Watchface,
         setSelectedTab = { },
         source = AppstoreSourceService.defaultSources.first(),
         appstoreSources = AppstoreSourceService.defaultSources,
         setSelectedSource = { },
         searchQuery = "",
         setSearchQuery = { },
         searchResults = Outcome.Progress(),
         navigator = null,
         homePageState = Outcome.Success(Json.decodeFromString(string)),
         onRefresh = {},
         navigateToCollection = { }
      )
   }
}
