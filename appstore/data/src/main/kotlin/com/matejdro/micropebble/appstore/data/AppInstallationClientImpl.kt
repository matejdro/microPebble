package com.matejdro.micropebble.appstore.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.matejdro.micropebble.appstore.api.AppDownloadFailed
import com.matejdro.micropebble.appstore.api.AppInstallSource
import com.matejdro.micropebble.appstore.api.AppInstallationClient
import com.matejdro.micropebble.appstore.api.AppSideloadFailed
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.LockerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.buffer
import okio.sink
import okio.source
import si.inova.kotlinova.core.outcome.Outcome
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import kotlin.uuid.Uuid

private object AppInstallSourcesSerializer : Serializer<Map<Uuid, AppInstallSource>> {
   private val json = Json { ignoreUnknownKeys = true }
   override val defaultValue = emptyMap<Uuid, AppInstallSource>()

   override suspend fun readFrom(input: InputStream) =
      try {
         json.decodeFromString<Map<Uuid, AppInstallSource>>(input.readBytes().decodeToString())
      } catch (e: SerializationException) {
         Log.e("AppInstallSourcesSerializer.readFrom", "Failed to load appstore sources", e)
         defaultValue
      }

   override suspend fun writeTo(t: Map<Uuid, AppInstallSource>, output: OutputStream) = withContext(Dispatchers.IO) {
      output.write(json.encodeToString(t).encodeToByteArray())
   }
}

private val Context.appInstallSources by dataStore(fileName = "appInstallSources.json", serializer = AppInstallSourcesSerializer)

@Inject
@ContributesBinding(AppScope::class)
class AppInstallationClientImpl(
   private val lockerApi: LockerApi,
   private val context: Context,
) : AppInstallationClient {
   override val appInstallSources: Flow<Map<Uuid, AppInstallSource>> = context.appInstallSources.data

   override suspend fun install(url: URL, source: AppInstallSource?, tmpFileName: String): Outcome<Unit> =
      withContext(Dispatchers.IO) {
         if (source != null) {
            context.appInstallSources.updateData { it + (source.appId to source) }
         }

         val tmpFile = File.createTempFile(tmpFileName, "pbw")

         try {
            url.openConnection().getInputStream().use { input ->
               tmpFile.sink().buffer().use {
                  it.writeAll(input.source())
               }
            }
         } catch (e: IOException) {
            return@withContext Outcome.Error(AppDownloadFailed(e))
         }

         try {
            val result = lockerApi.sideloadApp(Path(tmpFile.absolutePath))
            if (result) {
               Outcome.Success(Unit)
            } else {
               if (source != null) {
                  context.appInstallSources.updateData { it - source.appId }
               }
               Outcome.Error(AppSideloadFailed())
            }
         } finally {
            tmpFile.delete()
         }
      }

   override suspend fun removeFromSources(id: Uuid) {
      context.appInstallSources.updateData { data ->
         data.toMutableMap().apply {
            remove(id)
         }
      }
   }

   override suspend fun updateSources(id: Uuid, newSource: AppInstallSource) {
      context.appInstallSources.updateData { data ->
         data.toMutableMap().apply {
            this[id] = newSource
         }
      }
   }

   override suspend fun getInstallationSource(appId: Uuid) = context.appInstallSources.data.first()[appId]
}
