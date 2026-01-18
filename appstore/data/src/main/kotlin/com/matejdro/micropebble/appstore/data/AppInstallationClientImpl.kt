package com.matejdro.micropebble.appstore.data

import com.matejdro.micropebble.appstore.api.AppDownloadFailed
import com.matejdro.micropebble.appstore.api.AppInstallState
import com.matejdro.micropebble.appstore.api.AppInstallationClient
import com.matejdro.micropebble.appstore.api.AppSideloadFailed
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.LockerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import okio.buffer
import okio.sink
import okio.source
import si.inova.kotlinova.core.outcome.Outcome
import java.io.File
import java.io.IOException
import java.net.URL

@Inject
@ContributesBinding(AppScope::class)
class AppInstallationClientImpl(
   private val lockerApi: LockerApi,
) : AppInstallationClient {
   override suspend fun install(url: URL, tmpFileName: String) = withContext(Dispatchers.IO) {
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
            Outcome.Success(AppInstallState.INSTALLED)
         } else {
            Outcome.Error(AppSideloadFailed())
         }
      } finally {
         tmpFile.delete()
      }
   }
}
