package com.matejdro.micropebble.logging

import android.content.Context
import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import org.tinylog.core.TinylogLoggingProvider
import org.tinylog.provider.ProviderRegistry
import java.io.File

@Inject
@ContributesBinding(AppScope::class)
class FileLoggingControllerImpl(
   private val context: Context,
) : FileLoggingController {
   override fun flush() {
      (ProviderRegistry.getLoggingProvider()!! as TinylogLoggingProvider).writers.forEach {
         it.flush()
      }
   }

   override fun getLogFolder(): File {
      return File(context.cacheDir, "logs")
   }

   override fun getDeviceInfo(): String {
      val pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
      val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { pInfo.longVersionCode } else { pInfo.versionCode }

      return "Manufacturer: ${android.os.Build.MANUFACTURER}\n" +
         "Device: ${android.os.Build.MODEL} (${android.os.Build.DEVICE})\n" +
         "OS: ${android.os.Build.VERSION.RELEASE}\n" +
         "OS Build: ${android.os.Build.FINGERPRINT}\n" +
         "SDK: ${android.os.Build.VERSION.SDK_INT}\n" +
         "App version: ${pInfo.versionName.orEmpty()} ($vCode)"
   }
}
