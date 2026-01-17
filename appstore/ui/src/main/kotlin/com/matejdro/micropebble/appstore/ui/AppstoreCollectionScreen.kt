package com.matejdro.micropebble.appstore.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.matejdro.micropebble.appstore.ui.common.WatchAppDisplay
import com.matejdro.micropebble.appstore.ui.common.appGridCells
import com.matejdro.micropebble.navigation.keys.AppstoreCollectionScreenKey
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@Inject
@InjectNavigationScreen
class AppstoreCollectionScreen(
   private val viewModel: AppstoreCollectionViewModel,
   private val navigator: Navigator,
) : Screen<AppstoreCollectionScreenKey>() {
   val appTileContentType = "appTileContentType"

   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   override fun Content(key: AppstoreCollectionScreenKey) {
      val apps = viewModel.getLazyPagingItems()

      val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

      Scaffold(
         modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
            MediumTopAppBar(
               title = { Text(key.title) },
               scrollBehavior = scrollBehavior,
            )
         }
      ) { contentPadding ->
         LazyVerticalGrid(
            columns = appGridCells,
            modifier = Modifier
               .fillMaxSize()
               .padding(contentPadding)
               .padding(8.dp)
               .clip(CardDefaults.shape),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            // contentPadding = PaddingValues(vertical = 8.dp)
         ) {
            items(apps.itemCount, contentType = { appTileContentType }) { index ->
               apps[index]?.let { WatchAppDisplay(it, navigator, appstoreSource = key.appstoreSource) }
            }
            if (!apps.loadState.isIdle) {
               item(span = { GridItemSpan(maxLineSpan) }) {
                  Row(horizontalArrangement = Arrangement.Center) {
                     CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                  }
               }
            }
         }
      }
   }
}
