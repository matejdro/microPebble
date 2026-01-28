package com.matejdro.micropebble.appstore.data

import androidx.datastore.core.DataStore
import com.matejdro.micropebble.appstore.api.ApiClient
import com.matejdro.micropebble.appstore.api.AppInstallSource
import com.matejdro.micropebble.appstore.api.AppInstallationClient
import com.matejdro.micropebble.appstore.api.AppStatus
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.common.util.compareTo
import com.matejdro.micropebble.common.util.parseVersionString
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dispatch.core.withIO
import io.ktor.utils.io.readTo
import io.rebble.libpebblecommon.connection.LockerApi
import kotlinx.coroutines.flow.first
import kotlinx.io.asSink
import kotlinx.io.files.Path
import si.inova.kotlinova.core.outcome.Outcome
import java.io.File
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class AppInstallationClientImpl(
   private val lockerApi: LockerApi,
   private val api: ApiClient,
   private val appInstallSourcesStore: DataStore<Map<Uuid, AppInstallSource>>,
) : AppInstallationClient {
   override val appInstallSources = appInstallSourcesStore.data

   override suspend fun install(url: String, source: AppInstallSource?, tmpFileName: String) = withIO {
      if (source != null) {
         appInstallSourcesStore.updateData { it + (source.appId to source) }
      }

      val tmpFile = File.createTempFile(tmpFileName, "pbw")

      try {
         tmpFile.outputStream().asSink().use { api.openInputStream(url).readTo(it) }
         lockerApi.sideloadApp(Path(tmpFile.absolutePath))
         Outcome.Success(Unit)
      } finally {
         tmpFile.delete()
      }
   }

   override suspend fun uninstall(uuid: Uuid) = withIO {
      val result = lockerApi.removeApp(uuid)
      removeFromSources(uuid)
      result
   }

   override suspend fun removeFromSources(id: Uuid) {
      appInstallSourcesStore.updateData { data ->
         data.toMutableMap().apply {
            remove(id)
         }
      }
   }

   override suspend fun updateSources(id: Uuid, newSource: AppInstallSource) {
      appInstallSourcesStore.updateData { data ->
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
      val response = api.fetchAppListing(updateSource, installSource) ?: return AppStatus.AppNotFound
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

   override suspend fun getInstallationSource(appId: Uuid) = appInstallSourcesStore.data.first()[appId]
}
