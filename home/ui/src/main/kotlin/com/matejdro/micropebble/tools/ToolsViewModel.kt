package com.matejdro.micropebble.tools

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.core.content.FileProvider
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.logging.FileLoggingController
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Stable
@Inject
@ContributesScopedService
class ToolsViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val context: Context,
   private val fileLoggingController: FileLoggingController,
) : SingleScreenViewModel<ToolsScreenKey>(resources.scope) {
   private val _appVersion = MutableStateFlow<String>("")
   val appVersion: StateFlow<String>
      get() = _appVersion

   private val _logSave = MutableStateFlow<Outcome<Uri?>>(Outcome.Success(null))
   val logSave: StateFlow<Outcome<Uri?>> = _logSave

   override fun onServiceRegistered() {
      actionLogger.logAction { "ToolsViewModel.onServiceRegistered()" }

      val pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
      _appVersion.value = pInfo.versionName.orEmpty()
   }

   fun getLogs() = resources.launchResourceControlTask(_logSave) {
      actionLogger.logAction { "ToolsViewModel.getLogs()" }

      val zipUri = withDefault {
         fileLoggingController.flush()

         val logFolder = fileLoggingController.getLogFolder()
         File(logFolder, "device.txt").writeText(fileLoggingController.getDeviceInfo())

         val logsZipFile = File(logFolder, "logs.zip")
         ZipOutputStream(FileOutputStream(logsZipFile).buffered()).use { zipOutputStream ->
            zipOutputStream.addAllLogsToZip(logFolder, logsZipFile)
         }

         FileProvider.getUriForFile(context, "com.matejdro.micropebble.logs", logsZipFile)
      }

      emit(Outcome.Success(zipUri))
   }

   private fun ZipOutputStream.addAllLogsToZip(logFolder: File, logsZipFile: File) {
      val buffer = ByteArray(ZIP_BUFFER_SIZE)

      for (logFile in logFolder.listFiles().orEmpty()) {
         if (logFile == logsZipFile) {
            continue
         }

         val zipEntry = ZipEntry(logFile.name)
         putNextEntry(zipEntry)
         FileInputStream(logFile).use { logFileInputStream ->
            var readBytes: Int
            while ((logFileInputStream.read(buffer).also { readBytes = it }) > 0) {
               write(buffer, 0, readBytes)
            }
         }
         closeEntry()
      }
   }

   fun resetLog() {
      actionLogger.logAction { "ToolsViewModel.resetLog()" }
      _logSave.value = Outcome.Success(null)
   }
}

private const val ZIP_BUFFER_SIZE = 1024
