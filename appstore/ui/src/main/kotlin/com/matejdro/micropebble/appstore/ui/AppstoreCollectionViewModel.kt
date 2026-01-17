package com.matejdro.micropebble.appstore.ui

import androidx.compose.runtime.Composable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.collection.AppstoreCollectionPage
import com.matejdro.micropebble.appstore.ui.common.getHttpClient
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.AppstoreCollectionScreenKey
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.request.get
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Inject
@ContributesScopedService
class AppstoreCollectionViewModel(
   resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
) : SingleScreenViewModel<AppstoreCollectionScreenKey>(resources.scope) {
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
            val page = getHttpClient().get(key.endpoint) {
               url {
                  parameters["offset"] = offset.toString()
                  parameters["limit"] = params.loadSize.toString()
               }
            }.body<AppstoreCollectionPage>()
            return LoadResult.Page(
               data = page.apps,
               prevKey = if (params.loadSize >= offset) null else offset - params.loadSize,
               nextKey = page.links.nextPage?.let { offset + params.loadSize }
            )
         }
      }
   }

   private lateinit var lazyPagingItems: LazyPagingItems<Application>

   /**
    * Cache the [LazyPagingItems] in the viewmodel so that the loading progress doesn't get reset on navigation.
    */
   @Composable
   fun getLazyPagingItems(): LazyPagingItems<Application> {
      actionLogger.logAction { "AppstoreCollectionViewModel.getLazyPagingItems()" }
      if (!this::lazyPagingItems.isInitialized) {
         lazyPagingItems = appPager.flow.collectAsLazyPagingItems()
      }
      return lazyPagingItems
   }
}
