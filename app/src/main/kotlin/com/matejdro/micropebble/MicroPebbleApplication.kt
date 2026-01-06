package com.matejdro.micropebble

import android.app.ActivityManager
import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.os.strictmode.Violation
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.matejdro.micropebble.di.ApplicationGraph
import com.matejdro.micropebble.di.ComponentFactory
import com.matejdro.micropebble.di.MainApplicationGraph
import com.matejdro.micropebble.logging.ErrorReportingKermitWriter
import com.matejdro.micropebble.logging.MultiLogcatLogger
import com.matejdro.micropebble.logging.TinyLogKermitWriter
import com.matejdro.micropebble.logging.TinyLogLogcatLogger
import dev.zacsweers.metro.createGraphFactory
import dispatch.core.DefaultDispatcherProvider
import dispatch.core.defaultDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.dispatchers.AccessCallbackDispatcherProvider
import si.inova.kotlinova.core.logging.AndroidLogcatLogger
import si.inova.kotlinova.core.logging.LogPriority
import si.inova.kotlinova.core.logging.LogcatLogger
import java.io.File
import co.touchlab.kermit.Logger as KermitLogger

open class MicroPebbleApplication : Application() {
   open val applicationGraph: ApplicationGraph by lazy {
      createGraphFactory<MainApplicationGraph.Factory>().create(this)
   }

   init {
      if (BuildConfig.DEBUG) {
         // Enable better coroutine stack traces on debug builds
         // this slows down coroutines, so it should not be enabled on release
         // using init instead of onCreate ensures that this is started before any content providers
         System.setProperty("kotlinx.coroutines.debug", "on")
      }
   }

   override fun onCreate() {
      super.onCreate()

      ComponentFactory.serviceFactories = applicationGraph.provideServiceFactories()
      ComponentFactory.receiverFactories = applicationGraph.provideReceiverFactories()

      if (!isMainProcess()) {
         // Do not perform any initialisation in other processes, they are usually library-specific
         return
      }

      setupLogging()

      enableStrictMode()

      DefaultDispatcherProvider.set(
         AccessCallbackDispatcherProvider(DefaultDispatcherProvider.get()) {
            if (BuildConfig.DEBUG) {
               error("Dispatchers not provided via coroutine scope.")
            }
         }
      )

      SingletonImageLoader.setSafe {
         ImageLoader.Builder(this)
            // Load Coil cache on the background thread
            // See https://github.com/coil-kt/coil/issues/1878
            .interceptorCoroutineContext(applicationGraph.getDefaultCoroutineScope().defaultDispatcher)
            .build()
      }

      applicationGraph.initLibPebble().apply {
         applicationGraph.getDefaultCoroutineScope().launch {
            val config = config.first()
            updateConfig(
               config.copy(
                  watchConfig = config.watchConfig.copy(
                     lanDevConnection = true,
                     preferBtClassicV2 = true,
                     verboseWatchManagerLogging = true
                  ),
                  bleConfig = config.bleConfig.copy(verbosePpogLogging = false)
               )
            )
         }
      }
      applicationGraph.initNotificationChannels()
   }

   private fun setupLogging() {
      // Logging situation with this app is a bit complicated:
      // logcat (the library) - used in the app part to log
      // Kermit - used in the LibPebble3 to log
      // Tinylog - used to create a persistent rolling file log
      // Both logcat and Kermit log into the Android's Logcat log, here we just need to wire them to also log
      // into Tinylog

      val directoryForLogs: File = applicationGraph.getFileLoggingController().getLogFolder()
         .also { it.mkdirs() }
      System.setProperty("tinylog.directory", directoryForLogs.getAbsolutePath())

      val loggingThread = applicationGraph.getTinyLogLoggingThread()

      val logcatLoggers = listOfNotNull(
         TinyLogLogcatLogger(loggingThread),
         if (BuildConfig.DEBUG) {
            AndroidLogcatLogger(minPriority = LogPriority.VERBOSE)
         } else {
            null
         }
      )
      LogcatLogger.install(MultiLogcatLogger(logcatLoggers))

      KermitLogger.addLogWriter(TinyLogKermitWriter(loggingThread))
      if (BuildConfig.DEBUG) {
         // There are still many false positives from Kermit, so this should only be debug-only for now
         KermitLogger.addLogWriter(ErrorReportingKermitWriter(applicationGraph.getErrorReporter()))
      }
   }

   private fun enableStrictMode() {
      // Also check on staging release build, if applicable
      // penaltyListener only supports P and newer, so we are forced to only enable StrictMode on those devices
      if (!BuildConfig.DEBUG) {
         return
      }

      StrictMode.setVmPolicy(
         VmPolicy.Builder()
            .detectActivityLeaks()
            .detectContentUriWithoutPermission()
            .detectFileUriExposure()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .detectCredentialProtectedWhileLocked()
            .detectImplicitDirectBoot()
            .detectUnsafeIntentLaunch()
            .run {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                  detectBlockedBackgroundActivityLaunch()
               } else {
                  this
               }
            }

            .penaltyListener(ContextCompat.getMainExecutor(this@MicroPebbleApplication)) { e ->
               reportStrictModePenalty(e)
            }
            .build()
      )

      StrictMode.setThreadPolicy(
         StrictMode.ThreadPolicy.Builder()
            .detectCustomSlowCalls()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .detectResourceMismatches()
            .detectUnbufferedIo()
            .penaltyListener(ContextCompat.getMainExecutor(this)) { e ->
               reportStrictModePenalty(e)
            }
            .build()
      )
   }

   private fun reportStrictModePenalty(e: Violation) {
      if (
         e.cause == null &&
         (
            STRICT_MODE_EXCLUSIONS.any {
               e.toString().contains(it)
            } ||
               e.stackTrace.any { stackTraceElement ->
                  STRICT_MODE_EXCLUSIONS.any {
                     stackTraceElement.toString().contains(it)
                  }
               }
            )
      ) {
         // Exclude some classes from strict mode, see STRICT_MODE_EXCLUSIONS below.
         return
      }

      if (BuildConfig.DEBUG) {
         throw e
      } else {
         applicationGraph.getErrorReporter().report(e)
      }
   }

   private fun isMainProcess(): Boolean {
      val activityManager = getSystemService<ActivityManager>()!!
      val myPid = android.os.Process.myPid()

      return activityManager.runningAppProcesses?.any {
         it.pid == myPid && packageName == it.processName
      } == true
   }
}

private val STRICT_MODE_EXCLUSIONS = listOf(
   "UnixSecureDirectoryStream", // https://issuetracker.google.com/issues/270704908
   "UnixDirectoryStream", // https://issuetracker.google.com/issues/270704908,
   "SurfaceControl.finalize", // https://issuetracker.google.com/issues/167533582
   "InsetsSourceControl", // https://issuetracker.google.com/issues/307473789
   "io.rebble.libpebblecommon.di.LibPebbleModuleKt.initKoin", // libPebble init is doing a lot of main thread reads
   "readFromParcel", // Likely originating somewhere from within the framework. Not enough info to diagnose it.
   "MainDispatcherLoader", // Needs to class load main dispatcher class from disk
   "miui", // MIUI sometimes makes disk access calls on the OS side. We cannot control those.
   "TurboSchedMonitorImpl", // Part of some OS distributions, such as MIUI. We cannot control those, so exclude them.
   "AutofillClientController", // Autofill is starting unsafe intents. Nothing we can do.
   "mediatek.boostfwk",
)
