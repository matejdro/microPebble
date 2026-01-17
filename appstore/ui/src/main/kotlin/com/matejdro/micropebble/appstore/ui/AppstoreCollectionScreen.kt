package com.matejdro.micropebble.appstore.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.matejdro.micropebble.appstore.ui.common.WatchAppDisplay
import com.matejdro.micropebble.navigation.keys.AppstoreCollectionScreenKey
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@Inject
@InjectNavigationScreen
class AppstoreCollectionScreen(
   private val viewModel: AppstoreCollectionViewModel,
   private val navigator: Navigator,
) : Screen<AppstoreCollectionScreenKey>() {
   val categoryHeaderContentType = "categoryHeaderContentType"
   val appTileContentType = "appTileContentType"

   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   override fun Content(key: AppstoreCollectionScreenKey) {
      val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
      val state = viewModel.state.collectAsStateWithLifecycleAndBlinkingPrevention().value

      LaunchedEffect(Unit) { viewModel.load() }

      Scaffold(
         modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
            MediumTopAppBar(
               title = {
                  Text(key.title)
               },
               actions = {
                  IconButton(
                     onClick = { viewModel.previousPage() },
                     enabled = state is Outcome.Success && (viewModel.page - 1 in viewModel.collections.indices)
                  ) {
                     Icon(
                        painter = painterResource(R.drawable.outline_chevron_backward_24),
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp)
                     )
                  }
                  Text(
                     (viewModel.page + 1).toString(), modifier = Modifier.widthIn(min = 16.dp), textAlign = TextAlign.Center
                  )
                  val canGoToNext =
                     state is Outcome.Success && (!viewModel.hasFoundEnd || viewModel.page + 1 in viewModel.collections.indices)
                  IconButton(
                     onClick = { viewModel.nextPage() }, enabled = canGoToNext
                  ) {
                     Icon(
                        painter = painterResource(R.drawable.outline_chevron_forward_24),
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp)
                     )
                  }
               },
               scrollBehavior = scrollBehavior,
            )
         }
      ) { contentPadding ->
         PullToRefreshBox(
            isRefreshing = state is Outcome.Progress,
            onRefresh = { viewModel.reload() },
            modifier = Modifier
               .fillMaxWidth()
               .padding(contentPadding),
         ) {
            LazyVerticalGrid(
               columns = GridCells.Fixed(2),
               modifier = Modifier.fillMaxSize(),
               verticalArrangement = Arrangement.spacedBy(8.dp),
               horizontalArrangement = Arrangement.spacedBy(8.dp),
               contentPadding = PaddingValues(8.dp)
            ) {
               if (state is Outcome.Success) {
                  val data = state.data
                  items(data.apps, contentType = { appTileContentType }) {
                     WatchAppDisplay(it, navigator, appstoreSource = key.appstoreSource)
                  }
               }
            }
         }
      }
   }
}
