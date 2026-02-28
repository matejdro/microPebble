package com.matejdro.micropebble.pbw

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.matejdro.micropebble.navigation.keys.FirmwareUpdateScreenKey
import com.matejdro.micropebble.navigation.keys.HomeScreenKey
import com.matejdro.micropebble.navigation.keys.WatchappListKey
import com.matejdro.micropebble.navigation.keys.common.InputFile
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Inject
class FileInstallHandler(private val context: Context) {
   suspend fun getTargetScreens(intent: Intent): List<ScreenKey> {
      val uri = intent.data
      if (uri?.scheme != "content") {
         return emptyList()
      }

      return if (uri.path?.endsWith(".pbw") == true) {
         listOf(HomeScreenKey, WatchappListKey(InputFile(uri, uri.pathSegments.last())))
      } else if (uri.path?.endsWith(".pbz") == true) {
         listOf(FirmwareUpdateScreenKey(pbzFile = InputFile(uri, uri.pathSegments.last())))
      } else {
         val fileName = getFileName(uri) ?: return emptyList()
         if (fileName.endsWith(".pbw")) {
            listOf(HomeScreenKey, WatchappListKey(InputFile(uri, fileName)))
         } else if (fileName.endsWith(".pbz")) {
            listOf(FirmwareUpdateScreenKey(pbzFile = InputFile(uri, fileName)))
         } else {
            emptyList()
         }
      }
   }

   private suspend fun getFileName(uri: Uri): String? = withDefault {
      val projection = arrayOf<String?>(MediaStore.MediaColumns.DISPLAY_NAME)
      context.contentResolver.query(uri, projection, null, null, null).use { cursor ->
         if (cursor?.moveToFirst() != true) return@use null
         cursor.getString(0)
      }
   }
}
