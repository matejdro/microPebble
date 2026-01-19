package com.matejdro.micropebble.appstore.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.matejdro.micropebble.appstore.api.AlgoliaData
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import kotlin.uuid.Uuid

private object AppstoreSourcesSerializer : Serializer<List<AppstoreSource>> {
   private val json = Json { ignoreUnknownKeys = true }
   override val defaultValue = listOf(
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
   override val sources: Flow<List<AppstoreSource>>
      get() = context.appstoreSourcesStore.data

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
