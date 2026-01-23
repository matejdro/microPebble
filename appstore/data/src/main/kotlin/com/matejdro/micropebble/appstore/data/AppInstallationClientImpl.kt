package com.matejdro.micropebble.appstore.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.matejdro.micropebble.appstore.api.ApiClient
import com.matejdro.micropebble.appstore.api.AppDownloadFailed
import com.matejdro.micropebble.appstore.api.AppInstallSource
import com.matejdro.micropebble.appstore.api.AppInstallationClient
import com.matejdro.micropebble.appstore.api.AppSideloadFailed
import com.matejdro.micropebble.appstore.api.AppStatus
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.application.ApplicationList
import com.matejdro.micropebble.common.util.compareTo
import com.matejdro.micropebble.common.util.joinUrls
import com.matejdro.micropebble.common.util.parseVersionString
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.rebble.libpebblecommon.connection.LockerApi
import kotlinx.coroutines.Dispatchers
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
   private val api: ApiClient,
) : AppInstallationClient {
   override val appInstallSources = context.appInstallSources.data

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

   override suspend fun isAppUpdatable(installSource: AppInstallSource?, sources: List<AppstoreSource>): AppStatus {
      if (installSource == null) {
         return AppStatus.NotUpdatable
      }
      val updateSource = sources.findFor(installSource) ?: return AppStatus.MissingSource
      val currentVersion =
         lockerApi.getLockerApp(installSource.appId).first()?.properties?.version?.let { parseVersionString(it) }
            ?: return AppStatus.Error
      val response = fetchAppListing(updateSource, installSource) ?: return AppStatus.AppNotFound
      val latestVersion = parseVersionString(response.latestRelease.version) ?: return AppStatus.Error
      return if (latestVersion > currentVersion) {
         AppStatus.Updatable(currentVersion, latestVersion)
      } else {
         AppStatus.UpToDate
      }
   }

   override suspend fun isAppUpdatable(uuid: Uuid, sources: List<AppstoreSource>) =
      isAppUpdatable(getInstallationSource(uuid), sources)

   private fun List<AppstoreSource>.findFor(installSource: AppInstallSource) =
      find { it.enabled && it.id == installSource.sourceId }

   private suspend fun fetchAppListing(updateSource: AppstoreSource, installSource: AppInstallSource): Application? =
      runCatching {
         api.http.get(updateSource.url.joinUrls("/v1/apps/id/${installSource.storeId}")).body<ApplicationList>().data.first()
      }.getOrNull()

   override suspend fun getInstallationSource(appId: Uuid) = context.appInstallSources.data.first()[appId]
}
