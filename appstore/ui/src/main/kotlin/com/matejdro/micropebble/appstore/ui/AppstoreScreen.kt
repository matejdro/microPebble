package com.matejdro.micropebble.appstore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.logging.logcat
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@Inject
@InjectNavigationScreen
class AppstoreScreen(
   private val viewModel: AppstoreViewModel,
) : Screen<AppstoreScreenKey>() {
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   override fun Content(key: AppstoreScreenKey) {
      val state = viewModel.homePageState.collectAsStateWithLifecycleAndBlinkingPrevention().value

      LaunchedEffect(viewModel.selectedTab) { viewModel.loadHomePage() }

      Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
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
                  contentPadding = PaddingValues(8.dp),
               ) {
                  var totalIndex = 0
                  for (collection in data.collections) {
                     stickyHeader(collection.slug) {
                        Box(
                           modifier = Modifier
                              .fillMaxWidth()
                              .background(MaterialTheme.colorScheme.background)
                        ) {
                           Text(collection.name, Modifier.padding(8.dp), style = MaterialTheme.typography.headlineMedium)
                        }
                     }
                     for (appId in collection.appIds) {
                        item(totalIndex++) {
                           val app = data.applicationsById[appId]
                           if (app == null) {
                              Text(
                                 "App with ID $appId not found", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error
                              )
                           } else {
                              WatchAppDisplay(app)
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Composable
   private fun WatchAppDisplay(app: Application) {
      OutlinedButton(
         onClick = {
            logcat { "${app.title} clicked" }
         },
         shape = CardDefaults.outlinedShape,
         modifier = Modifier.fillMaxSize(),
      ) {
         Column(
            Modifier
               .fillMaxSize()
               .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
         ) {
            app.screenshotImages.firstOrNull()?.let {
               AsyncImage(
                  model = it.medium,
                  contentDescription = "App image for ${app.title}",
                  modifier = Modifier
                     .fillMaxWidth()
                     .padding(bottom = 8.dp)
                     .clip(CardDefaults.shape),
                  contentScale = ContentScale.FillWidth,
               )
            }
            Text(
               app.title,
               textAlign = TextAlign.Center,
               maxLines = 1,
               overflow = TextOverflow.Ellipsis,
               style = MaterialTheme.typography.titleMedium
            )
         }
      }
   }
}
