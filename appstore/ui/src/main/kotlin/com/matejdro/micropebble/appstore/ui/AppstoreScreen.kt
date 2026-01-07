package com.matejdro.micropebble.appstore.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
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
      val state = viewModel.loadingState.collectAsStateWithLifecycleAndBlinkingPrevention().value
      var selectedTab by remember { mutableStateOf(ApplicationType.Watchface) }

      var isRefreshing by remember { mutableStateOf(false) }
      LaunchedEffect(Unit) { viewModel.reloadHomePage() }

      Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
         TabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
               selectedTab == ApplicationType.Watchface,
               onClick = { selectedTab = ApplicationType.Watchface },
               modifier = Modifier.sizeIn(minHeight = 48.dp)
            ) {
               Text(stringResource(R.string.watchface))
            }

            Tab(
               selectedTab == ApplicationType.Watchapp,
               onClick = { selectedTab = ApplicationType.Watchapp },
               modifier = Modifier.sizeIn(minHeight = 48.dp)
            ) {
               Text(stringResource(R.string.watchapp))
            }
         }
         ProgressErrorSuccessScaffold(state) {
            if (state is Outcome.Success) {
               val data = state.data
               PullToRefreshBox(isRefreshing = isRefreshing, { viewModel.reloadHomePage() }) {
                  LazyColumn(Modifier.fillMaxSize()) {
                     for (app in data.applications) {
                        item(app.id) {
                           Text(app.title, Modifier.padding(16.dp))
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
