package com.matejdro.micropebble.appstore.api

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface AppstoreSourceService {
   val isDefault: Flow<Boolean>
   val sources: Flow<List<AppstoreSource>>
   val enabledSources: Flow<List<AppstoreSource>>
   suspend fun reorderSource(source: AppstoreSource, newIndex: Int)
   suspend fun addSource(source: AppstoreSource)
   suspend fun replaceSource(oldSource: AppstoreSource, source: AppstoreSource)
   suspend fun restoreSources()
   suspend fun removeSource(source: AppstoreSource)
   suspend fun find(id: Uuid): AppstoreSource?
   suspend fun find(predicate: (AppstoreSource) -> Boolean): AppstoreSource?

   companion object {
      val defaultSources = listOf(
         AppstoreSource(
            id = Uuid.parse("a7f9e6d9-0a47-4540-83a8-672d5c5f9139"),
            url = "https://appstore-api.rebble.io/api",
            name = "Rebble",
            algoliaData = AlgoliaData(
               appId = "7683OW76EQ",
               apiKey = "252f4938082b8693a8a9fc0157d1d24f",
               indexName = "rebble-appstore-production"
            ),
         ),
         AppstoreSource(
            id = Uuid.parse("ddbec6a1-8ea1-42cc-8dee-b0373fbaa5bd"),
            url = "https://appstore-api.repebble.com/api",
            name = "Core Devices",
            algoliaData = AlgoliaData(
               appId = "GM3S9TRYO4",
               apiKey = "0b83b4f8e4e8e9793d2f1f93c21894aa",
               indexName = "apps"
            ),
         ),
      )
   }
}
