package com.matejdro.micropebble.appstore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.coerceAtLeast
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
import com.matejdro.micropebble.appstore.ui.common.getIcon
import com.matejdro.micropebble.appstore.ui.keys.AppstoreScreenKey
import com.matejdro.micropebble.navigation.keys.AppstoreSourcesScreenKey
import com.matejdro.micropebble.ui.components.BasicExposedDropdownMenuBox
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import com.matejdro.micropebble.ui.errors.NoSourcesDisplay
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.metadata.WatchType
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

      val state = viewModel.state.collectAsStateWithLifecycle().value

      AppstoreScreenScaffold(
         selectedTab = state.selectedTab,
         setSelectedTab = viewModel::setSelectedTab,
         source = state.appstoreSource,
         setSelectedSource = viewModel::setAppstoreSource,
         platformFilter = state.platformFilter,
         setPlatformFilter = viewModel::setPlatformFilter,
         appstoreSources = appstoreSources,
         searchQuery = state.searchQuery,
         setSearchQuery = viewModel::setSearchQuery,
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
   setSelectedSource: (AppstoreSource) -> Unit,
   platformFilter: WatchType?,
   setPlatformFilter: (WatchType?) -> Unit,
   appstoreSources: List<AppstoreSource>,
   searchQuery: String,
   setSearchQuery: (String) -> Unit,
   searchResults: Outcome<List<AlgoliaApplication>>?,
   navigator: Navigator?,
   homePageState: Outcome<AppstoreHomePage>?,
   onRefresh: () -> Unit,
   navigateToCollection: (AppstoreCollection) -> Unit,
) {
   var bottomPanelExpanded by remember { mutableStateOf(false) }
   Scaffold(
      topBar = {
         TypeSelector(
            selectedTab = selectedTab,
            setSelectedTab = setSelectedTab,
            onMoreClick = {
               bottomPanelExpanded = true
            }
         )
      }
   ) { contentPadding ->
      // The SearchBox has a little bit of forced padding over it which stacks with the forced padding on the bottom of the
      // TopAppBar, which when not compensated for looks bad.
      val realPadding = object : PaddingValues by contentPadding {
         override fun calculateTopPadding() = (contentPadding.calculateTopPadding() - 8.dp).coerceAtLeast(0.dp)
      }
      Column(
         Modifier
            .padding(realPadding)
            .consumeWindowInsets(contentPadding)
      ) {
         var lastLayoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
         val gridState = rememberLazyStaggeredGridState()
         var searchActive by rememberSaveable { mutableStateOf(false) }
         AppstoreHomepage(
            navigator = navigator,
            state = homePageState,
            onRefresh = onRefresh,
            navigateToCollection = navigateToCollection,
            platformFilter = platformFilter,
            appstoreSource = source,
            onGloballyPositioned = {
               lastLayoutCoordinates = it
            },
            gridState = gridState,
            {
               if (source?.algoliaData != null) {
                  item(span = StaggeredGridItemSpan.FullLine) {
                     Box(
                        Modifier
                           .align(Alignment.CenterHorizontally)
                           // Because the search box is in a lazy grid, its height must be constrained otherwise it tries to
                           // expand too large.
                           // The height is smaller than the screen height, so if no maximum is available, screen height is
                           // pretty reasonable.
                           .requiredHeightIn(
                              max = with(LocalDensity.current) { lastLayoutCoordinates?.size?.height?.toDp() }
                                 ?: LocalConfiguration.current.screenHeightDp.dp,
                           )
                     ) {
                        AppsSearchBox(
                           searchQuery = searchQuery,
                           setSearchQuery = setSearchQuery,
                           searchResults = searchResults,
                           navigator = navigator,
                           source = source,
                           platformFilter,
                           searchExpanded = searchActive,
                           onSearchExpandedChange = {
                              searchActive = it
                              if (it) {
                                 gridState.requestScrollToItem(0)
                              }
                           },
                           modifier = Modifier.align(Alignment.Center),
                        )
                     }
                  }
               }
            },
            scrollEnabled = !searchActive,
         )
      }

      if (bottomPanelExpanded) {
         ModalBottomSheet({ bottomPanelExpanded = false }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
               BasicExposedDropdownMenuBox(
                  textFieldValue = source?.name ?: stringResource(R.string.no_source_selected),
                  modifier = Modifier.fillMaxWidth(),
                  textFieldLeadingIcon = { Icon(painterResource(R.drawable.ic_appstore_source), contentDescription = null) },
                  textFieldLabel = { Text(stringResource(R.string.appstore_source)) },
               ) {
                  for (source in appstoreSources.filter { it.enabled }) {
                     DropdownMenuItem(
                        text = { Text(source.name) },
                        onClick = { setSelectedSource(source) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                     )
                  }
               }
               BasicExposedDropdownMenuBox(
                  textFieldValue = platformFilter?.codename ?: stringResource(R.string.all),
                  modifier = Modifier.fillMaxWidth(),
                  textFieldLeadingIcon = {
                     Icon(
                        painterResource(platformFilter?.getIcon() ?: R.drawable.ic_apps),
                        contentDescription = null
                     )
                  },
                  textFieldLabel = { Text(stringResource(R.string.platform_filter)) },
               ) {
                  for (platform in listOf(null) + WatchType.entries) {
                     DropdownMenuItem(
                        text = { Text(platform?.codename ?: stringResource(R.string.all)) },
                        onClick = { setPlatformFilter(platform) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        leadingIcon = {
                           Icon(
                              painterResource(platform?.getIcon() ?: R.drawable.ic_apps),
                              contentDescription = null
                           )
                        }
                     )
                  }
               }
            }
         }
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppsSearchBox(
   searchQuery: String,
   setSearchQuery: (String) -> Unit,
   searchResults: Outcome<List<AlgoliaApplication>>?,
   navigator: Navigator?,
   source: AppstoreSource,
   platformFilter: WatchType?,
   searchExpanded: Boolean,
   onSearchExpandedChange: (Boolean) -> Unit,
   modifier: Modifier = Modifier,
) {
   SearchBar(
      inputField = {
         SearchBarDefaults.InputField(
            query = searchQuery,
            onQueryChange = setSearchQuery,
            onSearch = {},
            expanded = searchExpanded,
            onExpandedChange = onSearchExpandedChange,
            placeholder = { Text(stringResource(R.string.search)) },
            leadingIcon = {
               Icon(painterResource(R.drawable.ic_search), contentDescription = null)
            }
         )
      },
      expanded = searchExpanded,
      onExpandedChange = onSearchExpandedChange,
      modifier = modifier,
      windowInsets = WindowInsets(left = 8.dp, right = 8.dp),
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
                  platform = platformFilter,
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
   onMoreClick: () -> Unit,
) {
   TopAppBar(
      title = {
         SecondaryTabRow(selectedTabIndex = selectedTab.ordinal) {
            LeadingIconTab(
               selectedTab == ApplicationType.Watchface,
               onClick = { setSelectedTab(ApplicationType.Watchface) },
               text = {
                  Text(stringResource(R.string.watchface))
               },
               icon = {
                  Icon(painterResource(R.drawable.ic_watchfaces), contentDescription = null)
               },
            )

            LeadingIconTab(
               selectedTab == ApplicationType.Watchapp,
               onClick = { setSelectedTab(ApplicationType.Watchapp) },
               text = {
                  Text(stringResource(R.string.watchapp))
               },
               icon = {
                  Icon(painterResource(R.drawable.ic_apps), contentDescription = null)
               },
            )
         }
      },
      actions = {
         IconButton(onClick = onMoreClick, modifier = Modifier.width(48.dp)) {
            Icon(painterResource(R.drawable.ic_more), contentDescription = null, modifier = Modifier.sizeIn(minWidth = 24.dp))
         }
      }
   )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppstoreHomepage(
   navigator: Navigator?,
   state: Outcome<AppstoreHomePage>?,
   onRefresh: () -> Unit,
   navigateToCollection: (AppstoreCollection) -> Unit,
   platformFilter: WatchType?,
   appstoreSource: AppstoreSource?,
   onGloballyPositioned: (LayoutCoordinates) -> Unit,
   gridState: LazyStaggeredGridState,
   topItem: LazyStaggeredGridScope.() -> Unit,
   scrollEnabled: Boolean,
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
               .clip(CardDefaults.shape)
               .onGloballyPositioned(onGloballyPositioned),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = gridState,
            userScrollEnabled = scrollEnabled,
         ) {
            topItem()
            for (collection in data.collections) {
               item(collection.slug, contentType = categoryHeaderContentType, span = StaggeredGridItemSpan.FullLine) {
                  AppstoreHeader(navigateToCollection, collection)
               }
               for (appId in collection.appIds) {
                  val app = data.applicationsById[appId]
                  if (app != null) {
                     item(contentType = appTileContentType) {
                        WatchAppDisplay(app, navigator, appstoreSource = appstoreSource, platform = platformFilter)
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
         setSelectedSource = { },
         platformFilter = null,
         setPlatformFilter = {},
         appstoreSources = AppstoreSourceService.defaultSources,
         searchQuery = "",
         setSearchQuery = { },
         searchResults = Outcome.Progress(),
         navigator = null,
         homePageState = Outcome.Success(Json.decodeFromString(string)),
         onRefresh = {}
      ) { }
   }
}
