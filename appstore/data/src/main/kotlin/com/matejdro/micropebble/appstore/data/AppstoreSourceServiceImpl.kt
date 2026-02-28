package com.matejdro.micropebble.appstore.data

import androidx.datastore.core.DataStore
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.appstore.api.AppstoreSourceService.Companion.defaultSources
import com.matejdro.micropebble.appstore.data.serializers.AppstoreSourcesSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class AppstoreSourceServiceImpl(
   private val appstoreSourcesStore: DataStore<List<AppstoreSource>>,
) : AppstoreSourceService {
   override val isDefault: Flow<Boolean> = sources.map { defaultSources == it }
   override val sources: Flow<List<AppstoreSource>>
      get() = appstoreSourcesStore.data
   override val enabledSources: Flow<List<AppstoreSource>>
      get() = sources.map { it.filter { source -> source.enabled } }

   override suspend fun reorderSource(source: AppstoreSource, newIndex: Int) {
      appstoreSourcesStore.updateData { settings ->
         val data = settings.toMutableList()
         val index = data.indexOf(source)
         if (index < 0) {
            error("Source not found")
         }
         data.removeAt(index)
         data.add(newIndex, source)
         data
      }
   }

   override suspend fun addSource(source: AppstoreSource) {
      appstoreSourcesStore.updateData { settings ->
         val data = settings.toMutableList()
         data.add(source)
         data
      }
   }

   override suspend fun replaceSource(oldSource: AppstoreSource, source: AppstoreSource) {
      appstoreSourcesStore.updateData { settings ->
         val data = settings.toMutableList()
         val index = data.indexOfFirst { it.id == oldSource.id }
         if (index == -1) {
            data.add(oldSource)
         } else {
            data[index] = source
         }
         data
      }
   }

   override suspend fun restoreSources() {
      appstoreSourcesStore.updateData { _ -> AppstoreSourcesSerializer.defaultValue }
   }

   override suspend fun removeSource(source: AppstoreSource) {
      appstoreSourcesStore.updateData { settings ->
         val data = settings.toMutableList()
         data.remove(source)
         data
      }
   }

   override suspend fun find(id: Uuid) = sources.first().find { it.id == id }
   override suspend fun find(predicate: (AppstoreSource) -> Boolean) = sources.first().find(predicate)
}
