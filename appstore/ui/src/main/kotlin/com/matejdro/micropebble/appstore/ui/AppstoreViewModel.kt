package com.matejdro.micropebble.appstore.ui

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Stable
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
import com.matejdro.micropebble.appstore.ui.keys.AppstoreCollectionScreenKey
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.metadata.WatchType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import kotlin.time.Duration.Companion.milliseconds

@Stable
@Serializable
data class AppstoreScreenState(
   val appstoreSource: AppstoreSource? = null,
   val selectedTab: ApplicationType = ApplicationType.Watchface,
   val platformFilter: WatchType? = null,
   val searchQuery: String = "",
) : Parcelable {
   override fun describeContents() = 0

   override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeString(Json.encodeToString(this))

   companion object CREATOR : Parcelable.Creator<AppstoreScreenState> {
      override fun createFromParcel(source: Parcel?) =
         runCatching { source?.readString()?.let { Json.decodeFromString<AppstoreScreenState>(it) } }.getOrNull()

      override fun newArray(size: Int) = arrayOfNulls<AppstoreScreenState?>(size)
   }
}

@Inject
@ContributesScopedService
class AppstoreViewModel(
   resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   appstoreSourceService: AppstoreSourceService,
   private val api: ApiClient,
) : SingleScreenViewModel<AppstoreScreenKey>(resources.scope) {
   private val reloadFlow = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

   val appstoreSources = appstoreSourceService.enabledSources

   private val _state by savedFlow { AppstoreScreenState() }
   val state: StateFlow<AppstoreScreenState> = _state

   val homePageState =
      state.transform { (source, tab, platform) ->
         if (source != null) {
            emit(Outcome.Progress())
            emit(getHomePage(source, tab, platform))
         }
      }

   val searchResults =
      state.debounce(100.milliseconds).transform {
         if (it.appstoreSource != null) {
            emit(Outcome.Progress())
            emit(getSearchResults(it))
         }
      }

   private val homePagesCache: MutableMap<Pair<Pair<AppstoreSource, ApplicationType>, WatchType?>, AppstoreHomePage> =
      mutableMapOf()

   override fun onServiceRegistered() {
      actionLogger.logAction { "AppstoreViewModel.onServiceRegistered()" }
      coroutineScope.launch {
         updateState { copy(appstoreSource = appstoreSources.first().firstOrNull()) }
      }
   }

   fun screenKeyFor(collection: AppstoreCollection): AppstoreCollectionScreenKey {
      actionLogger.logAction { "AppstoreViewModel.screenKeyFor()" }
      return AppstoreCollectionScreenKey(
         collection.name,
         state.value.appstoreSource!!.url.joinUrls(collection.links.apps.trimStart('/').removePrefix("api")),
         state.value.platformFilter?.codename,
         state.value.appstoreSource,
      )
   }

   private suspend fun getHomePage(
      source: AppstoreSource,
      tab: ApplicationType,
      platformFilter: WatchType?,
   ): Outcome<AppstoreHomePage> {
      homePagesCache[source to tab to platformFilter]?.let { return Outcome.Success(it) }
      try {
         val result = api.fetchHomePage(ensureAppstoreSource(), tab, platformFilter?.codename)
            .filterApps { it.isUnofficiallyCompatibleWith(platformFilter) }
         homePagesCache[source to tab to platformFilter] = result
         return Outcome.Success(result)
      } catch (e: Exception) {
         return Outcome.Error(DataParsingException(e.message, e))
      }
   }

   private suspend fun getSearchResults(state: AppstoreScreenState): Outcome<List<AlgoliaApplication>> {
      if (state.searchQuery.isBlank()) {
         return Outcome.Success(emptyList())
      }
      val algoliaData = state.appstoreSource?.algoliaData ?: return Outcome.Error(UnknownCauseException())
      val client = SearchClient(
         appId = algoliaData.appId,
         apiKey = algoliaData.apiKey,
      )
      val filters = TagFilters.of(
         buildList {
            add(TagFilters.of(state.selectedTab.searchTag))
            if (state.platformFilter != null) {
               add(TagFilters.of(state.platformFilter.codename))
            }
         }
      )
      val response: List<AlgoliaApplication> = client.searchSingleIndex(
         algoliaData.indexName,
         SearchParamsObject(state.searchQuery, tagFilters = filters)
      ).hits.mapNotNull { hit ->
         hit.additionalProperties?.let { api.json.decodeFromJsonElement(JsonObject(it)) }
      }
      return Outcome.Success(response)
   }

   private suspend fun ensureAppstoreSource(): AppstoreSource {
      if (state.value.appstoreSource == null) {
         updateState { copy(appstoreSource = appstoreSources.first().first()) }
      }
      return state.value.appstoreSource!!
   }

   fun reloadHomePage() {
      actionLogger.logAction { "AppstoreViewModel.reloadHomePage()" }
      homePagesCache.clear()
      reloadFlow.tryEmit(Unit)
   }

   fun setSelectedTab(value: ApplicationType) {
      actionLogger.logAction { "AppstoreViewModel.setSelectedTab($value)" }
      updateState { copy(selectedTab = value) }
   }

   fun setAppstoreSource(value: AppstoreSource?) {
      @Suppress("NullableToStringCall") // That's fine
      actionLogger.logAction { "AppstoreViewModel.setAppstoreSource($value)" }
      updateState { copy(appstoreSource = value) }
   }

   fun setPlatformFilter(value: WatchType?) {
      @Suppress("NullableToStringCall") // That's fine
      actionLogger.logAction { "AppstoreViewModel.setPlatformFilter($value)" }
      updateState { copy(platformFilter = value) }
   }

   fun setSearchQuery(value: String) {
      actionLogger.logAction { "AppstoreViewModel.setSearchQuery($value)" }
      updateState { copy(searchQuery = value) }
   }

   private inline fun updateState(block: AppstoreScreenState.() -> AppstoreScreenState) {
      _state.value = _state.value.block()
   }
}
