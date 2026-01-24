package com.matejdro.micropebble.appstore.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.appstore.api.AppstoreSourceService.Companion.defaultSources
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

private object AppstoreSourcesSerializer : Serializer<List<AppstoreSource>> {
   private val json = Json { ignoreUnknownKeys = true }
   override val defaultValue = AppstoreSourceService.defaultSources

   override suspend fun readFrom(input: InputStream) =
      try {
         json.decodeFromString<List<AppstoreSource>>(input.readBytes().decodeToString())
      } catch (e: SerializationException) {
         Log.e("AppstoreSourcesSerializer.readFrom", "Failed to load appstore sources", e)
         defaultValue
      }

   override suspend fun writeTo(t: List<AppstoreSource>, output: OutputStream) = withContext(Dispatchers.IO) {
      output.write(json.encodeToString(t).encodeToByteArray())
   }
}

private val Context.appstoreSourcesStore by dataStore(fileName = "appstoreSources.json", serializer = AppstoreSourcesSerializer)

@Inject
@ContributesBinding(AppScope::class)
class AppstoreSourceServiceImpl(
   private val context: Context,
) : AppstoreSourceService {
   override val isDefault: Flow<Boolean> = sources.map { defaultSources == it }
   override val sources: Flow<List<AppstoreSource>>
      get() = context.appstoreSourcesStore.data
   override val enabledSources: Flow<List<AppstoreSource>>
      get() = sources.map { it.filter { source -> source.enabled } }

   override suspend fun reorderSource(source: AppstoreSource, newIndex: Int) {
      context.appstoreSourcesStore.updateData { settings ->
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
      context.appstoreSourcesStore.updateData { settings ->
         val data = settings.toMutableList()
         data.add(source)
         data
      }
   }

   override suspend fun replaceSource(oldSource: AppstoreSource, source: AppstoreSource) {
      context.appstoreSourcesStore.updateData { settings ->
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
      context.appstoreSourcesStore.updateData { _ -> AppstoreSourcesSerializer.defaultValue }
   }

   override suspend fun removeSource(source: AppstoreSource) {
      context.appstoreSourcesStore.updateData { settings ->
         val data = settings.toMutableList()
         data.remove(source)
         data
      }
   }
}
