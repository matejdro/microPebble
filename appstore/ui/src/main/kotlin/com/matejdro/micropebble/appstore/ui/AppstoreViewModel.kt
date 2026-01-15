package com.matejdro.micropebble.appstore.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.home.AppstoreCollection
import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.AppstoreCollectionScreenKey
import com.matejdro.micropebble.navigation.keys.AppstoreScreenKey
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import kotlin.collections.set

val httpClient by lazy {
   HttpClient {
      install(ContentNegotiation) {
         json(
            Json {
               isLenient = true
               ignoreUnknownKeys = true
            }
         )
      }
   }
}

@Inject
@ContributesScopedService
class AppstoreViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
) : SingleScreenViewModel<AppstoreScreenKey>(resources.scope) {
   private val _loadingState = MutableStateFlow<Outcome<AppstoreHomePage>>(Outcome.Progress())
   val homePageState: StateFlow<Outcome<AppstoreHomePage>>
      get() = _loadingState

   private val homePages: MutableMap<ApplicationType, AppstoreHomePage> = mutableMapOf()

   var selectedTab by mutableStateOf(ApplicationType.Watchface)
   var apiUrl by mutableStateOf("https://appstore-api.rebble.io")

   fun loadHomePage() = resources.launchResourceControlTask(_loadingState) {
      actionLogger.logAction { "AppstoreViewModel.loadHomePage()" }
      homePages[selectedTab]?.let {
         emit(Outcome.Success(it))
         return@launchResourceControlTask
      }
      reloadHomePage()
   }

   fun reloadHomePage() = resources.launchResourceControlTask(_loadingState) {
      actionLogger.logAction { "AppstoreViewModel.reloadHomePage()" }
      try {
         val result = getHomePage(selectedTab)
         homePages[selectedTab] = result
         result.applicationsById
         emit(Outcome.Success(result))
      } catch (e: IllegalArgumentException) {
         emit(Outcome.Error(DataParsingException(e.message, e)))
      }
   }

   fun screenKeyFor(collection: AppstoreCollection): AppstoreCollectionScreenKey {
      actionLogger.logAction { "AppstoreViewModel.screenKeyFor()" }
      return AppstoreCollectionScreenKey(collection.name, apiUrl + collection.links.apps)
   }

   private suspend fun getHomePage(type: ApplicationType) =
      withContext(Dispatchers.IO) { httpClient }.get(apiUrl + type.apiEndpoint).body<AppstoreHomePage>()
}
