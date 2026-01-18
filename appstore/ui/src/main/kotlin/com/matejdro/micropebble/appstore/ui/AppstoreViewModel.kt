package com.matejdro.micropebble.appstore.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.SearchParamsObject
import com.algolia.client.model.search.TagFilters
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.appstore.api.store.application.AlgoliaApplication
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.home.AppstoreCollection
import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import com.matejdro.micropebble.appstore.ui.common.getHttpClient
import com.matejdro.micropebble.appstore.ui.common.json
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.common.util.joinUrls
import com.matejdro.micropebble.navigation.keys.AppstoreCollectionScreenKey
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.logging.logcat
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Inject
@ContributesScopedService
class AppstoreViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val appstoreSourceService: AppstoreSourceService,
) : SingleScreenViewModel<AppstoreScreenKey>(resources.scope) {
   private val _loadingState: MutableStateFlow<Outcome<AppstoreHomePage>> = MutableStateFlow(Outcome.Progress())
   val homePageState: StateFlow<Outcome<AppstoreHomePage>>
      get() = _loadingState
   private val _searchResultsState: MutableStateFlow<Outcome<List<AlgoliaApplication>>> = MutableStateFlow(Outcome.Progress())
   val searchResultState: StateFlow<Outcome<List<AlgoliaApplication>>>
      get() = _searchResultsState

   private val homePages: MutableMap<Pair<AppstoreSource, ApplicationType>, AppstoreHomePage> = mutableMapOf()

   var selectedTab by mutableStateOf(ApplicationType.Watchface)

   val appstoreSources
      get() = appstoreSourceService.sources
   var appstoreSource: AppstoreSource? by mutableStateOf(null)

   var searchQuery by mutableStateOf("")

   fun loadHomePage() = resources.launchResourceControlTask(_loadingState) {
      actionLogger.logAction { "AppstoreViewModel.loadHomePage()" }
      ensureAppstoreSource()
      homePages[appstoreSource to selectedTab]?.let {
         emit(Outcome.Success(it))
         return@launchResourceControlTask
      }
      reloadHomePage()
   }

   fun reloadHomePage() = resources.launchResourceControlTask(_loadingState) {
      actionLogger.logAction { "AppstoreViewModel.reloadHomePage()" }
      try {
         val result = getHomePage(selectedTab)
         homePages[ensureAppstoreSource() to selectedTab] = result
         result.applicationsById
         emit(Outcome.Success(result))
      } catch (e: IllegalArgumentException) {
         emit(Outcome.Error(DataParsingException(e.message, e)))
      }
   }

   fun screenKeyFor(collection: AppstoreCollection): AppstoreCollectionScreenKey {
      actionLogger.logAction { "AppstoreViewModel.screenKeyFor()" }
      return AppstoreCollectionScreenKey(
         collection.name,
         appstoreSource!!.url.joinUrls(collection.links.apps.trimStart('/').removePrefix("api")),
         appstoreSource
      )
   }

   fun loadSearchResults() = resources.launchResourceControlTask(_searchResultsState) {
      actionLogger.logAction { "AppstoreViewModel.loadSearchResults()" }
      if (searchQuery.isBlank()) {
         emit(Outcome.Success(emptyList()))
         return@launchResourceControlTask
      }
      val algoliaData = ensureAppstoreSource().algoliaData ?: return@launchResourceControlTask
      val client = SearchClient(
         appId = algoliaData.appId,
         apiKey = algoliaData.apiKey,
      )
      val response: List<AlgoliaApplication> = client.searchSingleIndex(
         algoliaData.indexName,
         SearchParamsObject(query = searchQuery, tagFilters = TagFilters.of(selectedTab.searchTag))
      ).hits.mapNotNull {
         it.additionalProperties?.let { content ->
            logcat { content.toString() }
            json.decodeFromJsonElement(JsonObject(content))
         }
      }
      logcat { "Found result count of $response" }
      emit(Outcome.Success(response))
   }

   suspend fun ensureAppstoreSource(): AppstoreSource {
      actionLogger.logAction { "AppstoreViewModel.ensureAppstoreSource()" }
      if (appstoreSource == null) {
         appstoreSource = appstoreSources.first().first()
      }
      return appstoreSource!!
   }

   private suspend fun getHomePage(type: ApplicationType) =
      getHttpClient().get(ensureAppstoreSource().url.joinUrls(type.apiEndpoint)).body<AppstoreHomePage>()
}
