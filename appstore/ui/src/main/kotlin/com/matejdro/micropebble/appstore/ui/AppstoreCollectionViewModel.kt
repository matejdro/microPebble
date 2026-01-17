package com.matejdro.micropebble.appstore.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.matejdro.micropebble.appstore.api.store.collection.AppstoreCollectionPage
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.AppstoreCollectionScreenKey
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Inject
@ContributesScopedService
class AppstoreCollectionViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
) : SingleScreenViewModel<AppstoreCollectionScreenKey>(resources.scope) {
   private val _collections = mutableListOf<AppstoreCollectionPage>()
   val collections
      get() = _collections
   private val _state = MutableStateFlow<Outcome<AppstoreCollectionPage>>(Outcome.Progress())
   val state: StateFlow<Outcome<AppstoreCollectionPage>>
      get() = _state
   var page by mutableIntStateOf(0)
   var hasFoundEnd by mutableStateOf(false)
      private set

   fun reload() {
      actionLogger.logAction { "AppstoreCollectionViewModel.reload()" }
      _collections.clear()
      load()
   }

   fun load() = resources.launchResourceControlTask(_state) {
      actionLogger.logAction { "AppstoreCollectionViewModel.load()" }
      emit(Outcome.Progress())
      while (page !in _collections.indices) {
         loadNextPage()
      }
      emit(Outcome.Success(_collections[page]))
   }

   suspend fun CoroutineResourceManager.ResourceControlBlock<AppstoreCollectionPage>.loadNextPage() {
      actionLogger.logAction { "AppstoreCollectionViewModel.loadNextPage()" }
      emit(Outcome.Progress())
      try {
         val result = getNextPage()
         if (result.links.nextPage == null) {
            hasFoundEnd = true
         }
         _collections.add(result)
         emit(Outcome.Success(result))
      } catch (e: IllegalArgumentException) {
         emit(Outcome.Error(DataParsingException(e.message, e)))
      }
   }

   private suspend fun getNextPage(): AppstoreCollectionPage {
      actionLogger.logAction { "AppstoreCollectionViewModel.getNextPage()" }
      return withContext(Dispatchers.IO) { httpClient }.get(
         _collections.lastOrNull()?.links?.nextPage ?: key.endpoint
      )
         .body<AppstoreCollectionPage>()
   }

   fun previousPage() {
      actionLogger.logAction { "AppstoreCollectionViewModel.previousPage()" }
      page--
      load()
   }

   fun nextPage() {
      actionLogger.logAction { "AppstoreCollectionViewModel.nextPage()" }
      page++
      load()
   }
}
