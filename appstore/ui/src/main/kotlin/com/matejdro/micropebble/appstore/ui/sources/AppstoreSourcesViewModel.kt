package com.matejdro.micropebble.appstore.ui.sources

import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.appstore.ui.keys.AppstoreCollectionScreenKey
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Inject
@ContributesScopedService
class AppstoreSourcesViewModel(
   private val resources: CoroutineResourceManager,
   private val sourceService: AppstoreSourceService,
   private val logger: ActionLogger,
) : SingleScreenViewModel<AppstoreCollectionScreenKey>(resources.scope) {
   val sources
      get() = sourceService.sources
   val isDefaultSources = sourceService.isDefault

   fun reorderSource(source: AppstoreSource, newIndex: Int) {
      logger.logAction { "AppstoreSourcesViewModel.reorderSource($source, $newIndex)" }
      resources.scope.launch {
         sourceService.reorderSource(source, newIndex)
      }
   }

   fun addSource(source: AppstoreSource) {
      logger.logAction { "AppstoreSourcesViewModel.addSource($source)" }
      resources.scope.launch {
         sourceService.addSource(source)
      }
   }

   fun replaceSource(oldSource: AppstoreSource, source: AppstoreSource) {
      logger.logAction { "AppstoreSourcesViewModel.replaceSource($oldSource, $source)" }
      resources.scope.launch {
         sourceService.replaceSource(oldSource, source)
      }
   }

   fun restoreSources() {
      logger.logAction { "AppstoreSourcesViewModel.restoreSources()" }
      resources.scope.launch {
         sourceService.restoreSources()
      }
   }

   fun removeSource(source: AppstoreSource) {
      logger.logAction { "AppstoreSourcesViewModel.removeSource($source)" }
      resources.scope.launch {
         sourceService.removeSource(source)
      }
   }
}
