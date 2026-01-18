package com.matejdro.micropebble.appstore.api

import kotlinx.coroutines.flow.Flow

interface AppstoreSourceService {
   val sources: Flow<List<AppstoreSource>>
   suspend fun reorderSource(source: AppstoreSource, newIndex: Int)
   suspend fun addSource(source: AppstoreSource)
   suspend fun replaceSource(oldSource: AppstoreSource, source: AppstoreSource)
   suspend fun restoreSources()
   suspend fun removeSource(source: AppstoreSource)
}
