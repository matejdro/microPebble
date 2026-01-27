package com.matejdro.micropebble.appstore.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.SearchParamsObject
import com.algolia.client.model.search.TagFilters
import com.matejdro.micropebble.appstore.api.ApiClient
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.appstore.api.store.application.AlgoliaApplication
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.home.AppstoreCollection
import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import com.matejdro.micropebble.appstore.api.store.home.filterApps
import com.matejdro.micropebble.appstore.ui.common.isUnofficiallyCompatibleWith
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.common.util.joinUrls
import com.matejdro.micropebble.navigation.keys.AppstoreCollectionScreenKey
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.rebble.libpebblecommon.metadata.WatchType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import kotlin.time.Duration.Companion.milliseconds

@Inject
@ContributesScopedService
class AppstoreViewModel(
   resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   appstoreSourceService: AppstoreSourceService,
   private val api: ApiClient,
) : SingleScreenViewModel<AppstoreScreenKey>(resources.scope) {
   private val reloadFlow = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

   var selectedTab by mutableStateOf(ApplicationType.Watchface as ApplicationType)
   val homePageState =
      combineTransform(
         snapshotFlow { appstoreSource },
         snapshotFlow { selectedTab },
         reloadFlow,
         snapshotFlow { platformFilter }.debounce(100.milliseconds)
      ) { source, tab, _, platform ->
         if (source != null) {
            emit(Outcome.Progress())
            emit(getHomePage(source, tab, platform))
         }
      }

   val searchResults =
      combineTransform(
         snapshotFlow { searchQuery }.debounce(100.milliseconds),
         snapshotFlow { selectedTab },
         snapshotFlow { appstoreSource },
         snapshotFlow { platformFilter }.debounce(100.milliseconds)
      ) { query, tab, source, platform ->
         if (source != null) {
            emit(Outcome.Progress())
            emit(getSearchResults(query, source, tab, platform))
         }
      }

   private val homePagesCache: MutableMap<Pair<Pair<AppstoreSource, ApplicationType>, WatchType?>, AppstoreHomePage> =
      mutableMapOf()

   val appstoreSources = appstoreSourceService.enabledSources
   var appstoreSource: AppstoreSource? by mutableStateOf(null)

   var searchQuery by mutableStateOf("")

   var platformFilter: WatchType? by mutableStateOf(null)

   override fun onServiceRegistered() {
      actionLogger.logAction { "AppstoreViewModel.onServiceRegistered()" }
      coroutineScope.launch {
         appstoreSource = appstoreSources.first().firstOrNull()
      }
   }

   fun screenKeyFor(collection: AppstoreCollection): AppstoreCollectionScreenKey {
      actionLogger.logAction { "AppstoreViewModel.screenKeyFor()" }
      return AppstoreCollectionScreenKey(
         collection.name,
         appstoreSource!!.url.joinUrls(collection.links.apps.trimStart('/').removePrefix("api")),
         platformFilter?.codename,
         appstoreSource,
      )
   }

   private suspend fun getHomePage(
      source: AppstoreSource,
      tab: ApplicationType,
      platformFilter: WatchType?,
   ): Outcome<AppstoreHomePage> {
      homePagesCache[source to tab to platformFilter]?.let { return Outcome.Success(it) }
      try {
         val result = fetchHomePage(tab, platformFilter).filterApps { it.isUnofficiallyCompatibleWith(platformFilter) }
         homePagesCache[source to tab to platformFilter] = result
         return Outcome.Success(result)
      } catch (e: Exception) {
         return Outcome.Error(DataParsingException(e.message, e))
      }
   }

   private suspend fun getSearchResults(
      query: String,
      source: AppstoreSource,
      tab: ApplicationType,
      platform: WatchType?,
   ): Outcome<List<AlgoliaApplication>> {
      if (searchQuery.isBlank()) {
         return Outcome.Success(emptyList())
      }
      val algoliaData = source.algoliaData ?: return Outcome.Error(UnknownCauseException())
      val client = SearchClient(
         appId = algoliaData.appId,
         apiKey = algoliaData.apiKey,
      )
      val filters = TagFilters.of(
         buildList {
            add(TagFilters.of(tab.searchTag))
            if (platform != null) {
               add(TagFilters.of(platform.codename))
            }
         }
      )
      val response: List<AlgoliaApplication> = client.searchSingleIndex(
         algoliaData.indexName,
         SearchParamsObject(query, tagFilters = filters)
      ).hits.mapNotNull { hit ->
         hit.additionalProperties?.let { api.json.decodeFromJsonElement(JsonObject(it)) }
      }
      return Outcome.Success(response)
   }

   private suspend fun ensureAppstoreSource(): AppstoreSource {
      if (appstoreSource == null) {
         appstoreSource = appstoreSources.first().first()
      }
      return appstoreSource!!
   }

   private suspend fun fetchHomePage(type: ApplicationType, platformFilter: WatchType?): AppstoreHomePage {
      val baseUrl = ensureAppstoreSource().url.joinUrls(type.apiEndpoint)
      return api.http.get(baseUrl) {
         url {
            if (platformFilter != null) {
               parameters["hardware"] = platformFilter.codename
            }
         }
      }.body<AppstoreHomePage>()
   }

   fun reloadHomePage() {
      actionLogger.logAction { "AppstoreViewModel.reloadHomePage()" }
      homePagesCache.clear()
      reloadFlow.tryEmit(Unit)
   }
}
