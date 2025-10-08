package com.matejdro.micropebble.logging

import android.content.Context
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
      return "Device: ${android.os.Build.DEVICE}\n" +
         "OS: ${android.os.Build.VERSION.RELEASE}\n" +
         "SDK: ${android.os.Build.VERSION.SDK_INT}"
   }
}
