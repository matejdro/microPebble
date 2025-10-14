package com.matejdro.micropebble.crashreport

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference

/**
 * Based on the https://github.com/fvito/CrashReport/blob/9986dcc0abd5f90b03c2bcfa53120dad40617812/what-the-stack/src/main/java/com/haroldadmin/whatthesthis@CrashReportService.kt
 */
class CrashReportService : Service() {
   private var finishedWaitingForFile = false
   private var stopAfterWaitingForFile = false

   private val coroutineScope = MainScope()

   override fun onCreate() {
      super.onCreate()

      coroutineScope.launch {
         val earlyStartupCrashFile = getStartupCrashFile(this@CrashReportService)
         for (ignored in 0 until CRASH_FILE_POLL_MAX_COUNT) {
            if (earlyStartupCrashFile.exists()) {
               val crashData = earlyStartupCrashFile.readText()
               getStartupCrashFile(applicationContext).delete()

               // We cannot open the activity after app has already died because of
               // background activity start restrictions. Show notification instead.
               showCrashNotification(crashData, this@CrashReportService)

               stopSelf()

               break
            }
            delay(CRASH_FILE_POLL_INTERVAL_MS)
         }

         if (stopAfterWaitingForFile) {
            stopSelf()
         }
         finishedWaitingForFile = true
      }
   }

   /**
    * [Handler] that runs on the main thread to handle incoming processed uncaught
    * exceptions from [CrashReportExceptionHandler]
    *
    * We need to lazily initialize it because [getApplicationContext] returns null right
    * after the service is created.
    */
   private val handler by lazy { CrashReportHandler(applicationContext, WeakReference(this)) }

   /**
    * Runs when [CrashReportInitializer] calls [Context.bindService] to create a connection
    * to this service.
    *
    * It creates a [Messenger] that can be used to communicate with its [handler],
    * and returns its [IBinder].
    */
   override fun onBind(intent: Intent): IBinder? {
      val messenger = Messenger(handler)
      return messenger.binder
   }

   override fun onUnbind(intent: Intent?): Boolean {
      val value = super.onUnbind(intent)
      if (finishedWaitingForFile) {
         stopSelf()
      }
      stopAfterWaitingForFile = true
      return value
   }

   override fun onDestroy() {
      super.onDestroy()
      coroutineScope.cancel()
   }

   companion object {
      /**
       * Binder can only send a limited amount of text between components. Ensure we only send up to this characters.
       */
      const val CRASH_TEXT_LIMIT = 10_000

      fun getStartupCrashFile(context: Context): File {
         return File(context.cacheDir, "lastCrash.txt")
      }

      fun showCrashNotification(crashData: String, context: Context) {
         val notificationManager = context.getSystemService<NotificationManager>()!!

         if (!notificationManager.areNotificationsEnabled()) {
            Log.w(
               "CrashReport",
               "Cannot post crash notifications: App does not have a notification permission"
            )
            return
         }

         notificationManager.createNotificationChannel(
            NotificationChannel(
               CHANNEL_ID_CRASHES,
               context.getString(R.string.error_reports),
               NotificationManager.IMPORTANCE_HIGH
            )
         )

         val channel = notificationManager.getNotificationChannel(CHANNEL_ID_CRASHES)
         if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
            Log.w(
               "CrashReport",
               "Cannot post crash notifications: 'Crash' notification channel is disabled"
            )
            return
         }

         val id = CRASH_REQUEST_CODE xor System.currentTimeMillis().toInt()

         val notification = NotificationCompat.Builder(context, CHANNEL_ID_CRASHES)
            .setContentTitle(
               context.getString(
                  R.string.has_encountered_an_error,
                  context.getApplicationInfo().loadLabel(context.packageManager)
               )
            )
            .setContentText(context.getString(R.string.tap_to_see_more_info))
            .setSmallIcon(R.drawable.notification_error)
            .setContentIntent(
               PendingIntent.getActivity(
                  context,
                  id,
                  Intent(context, CrashReportActivity::class.java).putExtra(
                     CrashReportActivity.EXTRA_TEXT,
                     crashData.take(CRASH_TEXT_LIMIT)
                  ),
                  PendingIntent.FLAG_IMMUTABLE,
               ),
            ).build()

         notificationManager.notify(id, notification)
      }
   }
}

/**
 * A [Handler] that runs on the main thread of the service process to process
 * incoming uncaught exception messages.
 */
private class CrashReportHandler(
   private val applicationContext: Context,
   private val service: WeakReference<CrashReportService>,
) : Handler(Looper.getMainLooper()) {

   override fun handleMessage(msg: Message) {
      CrashReportService.getStartupCrashFile(applicationContext).delete()

      val data = msg.data.getString(CrashReportActivity.EXTRA_TEXT).orEmpty()
      if (applicationContext.getSystemService<NotificationManager>()!!.areNotificationsEnabled()) {
         CrashReportService.showCrashNotification(data, applicationContext)
      } else {
         applicationContext.startActivity(
            Intent(
               applicationContext,
               CrashReportActivity::class.java
            )
               .putExtra(CrashReportActivity.EXTRA_TEXT, data)
               .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
         )
      }

      service.get()?.stopSelf()
   }
}

private const val CHANNEL_ID_CRASHES = "CHANNEL_CRASHES"
private const val CRASH_FILE_POLL_MAX_COUNT = 10
private const val CRASH_FILE_POLL_INTERVAL_MS = 500L
private const val CRASH_REQUEST_CODE = 1234
