package com.matejdro.micropebble.appstore.ui.sources

import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.AppstoreCollectionScreenKey
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

   fun reorderSource(source: AppstoreSource, newIndex: Int) = resources.scope.launch {
      logger.logAction { "AppstoreSourcesViewModel.reorderSource($source, $newIndex)" }
      sourceService.reorderSource(source, newIndex)
   }

   fun addSource(source: AppstoreSource) = resources.scope.launch {
      logger.logAction { "AppstoreSourcesViewModel.addSource($source)" }
      sourceService.addSource(source)
   }

   fun replaceSource(oldSource: AppstoreSource, source: AppstoreSource) = resources.scope.launch {
      logger.logAction { "AppstoreSourcesViewModel.replaceSource($oldSource, $source)" }
      sourceService.replaceSource(oldSource, source)
   }

   fun restoreSources() = resources.scope.launch {
      logger.logAction { "AppstoreSourcesViewModel.restoreSources()" }
      sourceService.restoreSources()
   }

   fun removeSource(source: AppstoreSource) = resources.scope.launch {
      logger.logAction { "AppstoreSourcesViewModel.removeSource($source)" }
      sourceService.removeSource(source)
   }
}
