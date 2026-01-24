package com.matejdro.micropebble.appstore.api

import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome
import java.io.IOException
import java.net.URL
import kotlin.uuid.Uuid

enum class AppInstallState {
   INCOMPATIBLE,
   CAN_INSTALL,
   INSTALLED,
}

class AppSideloadFailed : CauseException(message = "App failed to sideload", isProgrammersFault = false)
class AppDownloadFailed(cause: IOException) : CauseException(message = "App download failed", cause)

interface AppInstallationClient {
   val appInstallSources: Flow<Map<Uuid, AppInstallSource>>
   suspend fun install(
      url: URL,
      source: AppInstallSource? = null,
      tmpFileName: String = Uuid.random().toString(),
   ): Outcome<Unit>
   suspend fun uninstall(uuid: Uuid): Boolean
   suspend fun getInstallationSource(appId: Uuid): AppInstallSource?
   suspend fun removeFromSources(id: Uuid)
   suspend fun updateSources(id: Uuid, newSource: AppInstallSource)
   suspend fun isAppUpdatable(
      installSource: AppInstallSource?,
      sources: List<AppstoreSource>,
   ): AppStatus

   suspend fun isAppUpdatable(
      uuid: Uuid,
      sources: List<AppstoreSource>,
   ): AppStatus
}
