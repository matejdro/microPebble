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
   suspend fun getTargetScreen(intent: Intent): ScreenKey? {
      val uri = intent.data
      if (uri?.scheme != "content") {
         return null
      }

      return if (uri.path?.endsWith(".pbw") == true) {
         HomeScreenKey(WatchappListKey(InputFile(uri, uri.pathSegments.last())))
      } else if (uri.path?.endsWith(".pbz") == true) {
         FirmwareUpdateScreenKey(pbzFile = InputFile(uri, uri.pathSegments.last()))
      } else {
         val fileName = getFileName(uri) ?: return null
         if (fileName.endsWith(".pbw") == true) {
            HomeScreenKey(WatchappListKey(InputFile(uri, fileName)))
         } else if (fileName.endsWith(".pbz") == true) {
            FirmwareUpdateScreenKey(pbzFile = InputFile(uri, fileName))
         } else {
            null
         }
      }
   }

   private suspend fun getFileName(uri: Uri): String? = withDefault {
      val projection = arrayOf<String?>(MediaStore.MediaColumns.DISPLAY_NAME)
      context.contentResolver.query(uri, projection, null, null, null).use {
         if (it?.moveToFirst() != true) return@use null
         it.getString(0)
      }
   }
}
