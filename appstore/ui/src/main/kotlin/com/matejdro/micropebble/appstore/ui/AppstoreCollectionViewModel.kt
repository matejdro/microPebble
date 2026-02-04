package com.matejdro.micropebble.appstore.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.matejdro.micropebble.appstore.api.ApiClient
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.collection.AppstoreCollectionPage
import com.matejdro.micropebble.appstore.api.store.collection.filterApps
import com.matejdro.micropebble.appstore.ui.common.isUnofficiallyCompatibleWith
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.appstore.ui.keys.AppstoreCollectionScreenKey
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.metadata.WatchType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class AppstoreCollectionViewModel(
   resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val api: ApiClient,
) : SingleScreenViewModel<AppstoreCollectionScreenKey>(resources.scope) {
   private val _platform = MutableStateFlow<WatchType?>(null)
   val platform: StateFlow<WatchType?> = _platform
   private val appPager = Pager(
      PagingConfig(
         pageSize = 10
      )
   ) {
      object : PagingSource<Int, Application>() {
         override fun getRefreshKey(state: PagingState<Int, Application>) =
            ((state.anchorPosition ?: 0) - state.config.initialLoadSize / 2).coerceAtLeast(0)

         override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Application> {
            val offset = (params.key ?: 0)
            val page = api.fetchCollection(key.platformFilter, key.endpoint, offset, params.loadSize).filterAppsByPlatform()
            return LoadResult.Page(
               data = page.apps,
               prevKey = if (params.loadSize >= offset) null else offset - params.loadSize,
               nextKey = page.links.nextPage?.let { offset + params.loadSize }
            )
         }
      }
   }

   private var lazyPagingItems: LazyPagingItems<Application>? = null

   override fun onServiceRegistered() {
      actionLogger.logAction { "AppstoreCollectionViewModel.onServiceRegistered()" }
      _platform.value = key.platformFilter?.let { WatchType.fromCodename(it) }
   }

   /**
    * Cache the [LazyPagingItems] in the viewmodel so that the loading progress doesn't get reset on navigation.
    */
   @Composable
   fun getLazyPagingItems(): LazyPagingItems<Application> {
      actionLogger.logAction { "AppstoreCollectionViewModel.getLazyPagingItems()" }
      return lazyPagingItems ?: appPager.flow.collectAsLazyPagingItems().also { lazyPagingItems = it }
   }

   private fun AppstoreCollectionPage.filterAppsByPlatform() = if (platform.value == null) {
      this
   } else {
      filterApps { it.isUnofficiallyCompatibleWith(platform.value) }
   }
}
