package com.matejdro.micropebble.appstore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.ui.common.WatchAppDisplay
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@Inject
@InjectNavigationScreen
class AppstoreScreen(
   private val viewModel: AppstoreViewModel,
   private val navigator: Navigator,
) : Screen<AppstoreScreenKey>() {
   val categoryHeaderContentType = "categoryHeaderContentType"
   val appTileContentType = "appTileContentType"

   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   override fun Content(key: AppstoreScreenKey) {
      val state = viewModel.homePageState.collectAsStateWithLifecycleAndBlinkingPrevention().value

      LaunchedEffect(viewModel.selectedTab) { viewModel.loadHomePage() }

      Column(Modifier.safeDrawingPadding()) {
         TabRow(selectedTabIndex = viewModel.selectedTab.ordinal) {
            Tab(
               viewModel.selectedTab == ApplicationType.Watchface,
               onClick = { viewModel.selectedTab = ApplicationType.Watchface },
               modifier = Modifier.sizeIn(minHeight = 48.dp)
            ) {
               Text(stringResource(R.string.watchface))
            }

            Tab(
               viewModel.selectedTab == ApplicationType.Watchapp,
               onClick = { viewModel.selectedTab = ApplicationType.Watchapp },
               modifier = Modifier.sizeIn(minHeight = 48.dp)
            ) {
               Text(stringResource(R.string.watchapp))
            }
         }
         PullToRefreshBox(
            isRefreshing = state is Outcome.Progress,
            onRefresh = { viewModel.reloadHomePage() },
            modifier = Modifier.fillMaxWidth(),
         ) {
            if (state is Outcome.Success) {
               val data = state.data
               LazyVerticalGrid(
                  columns = GridCells.Fixed(2),
                  modifier = Modifier.fillMaxSize(),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  contentPadding = PaddingValues(8.dp)
               ) {
                  var totalIndex = 0
                  for (collection in data.collections) {
                     stickyHeader(collection.slug, contentType = categoryHeaderContentType) {
                        Box(
                           modifier = Modifier
                              .fillMaxWidth()
                              .background(MaterialTheme.colorScheme.background)
                        ) {
                           TextButton(
                              onClick = {
                                 navigator.navigateTo(viewModel.screenKeyFor(collection))
                              },
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                 ) {
                                    Text(stringResource(R.string.seeAll))
                                    Icon(
                                       painter = painterResource(R.drawable.outline_chevron_forward_24),
                                       contentDescription = null,
                                       modifier = Modifier.padding(8.dp)
                                    )
                                 }
                              }
                           }
                        }
                     }
                     for (appId in collection.appIds) {
                        item(totalIndex++, contentType = appTileContentType) {
                           val app = data.applicationsById[appId]
                           if (app == null) {
                              Text(
                                 "App with ID $appId not found", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error
                              )
                           } else {
                              WatchAppDisplay(navigator, app)
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
