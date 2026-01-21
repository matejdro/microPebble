package com.matejdro.micropebble.appstore.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.matejdro.micropebble.appstore.api.updater.AppUpdaterNotificationIntent
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.IntoMap
import si.inova.kotlinova.core.logging.logcat

@Inject
@IntoMap
class AppUpdaterWorker(
   context: Context,
   params: WorkerParameters,
) : CoroutineWorker(context, params) {
   override suspend fun doWork(): Result {
      logcat { "AppUpdaterWorker is now doing something" }
      val channel = NotificationChannel(CHANNEL_ID_APP_UPDATE, "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
      val notificationManager = applicationContext.getSystemService<NotificationManager>() ?: return Result.failure()
      val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID_APP_UPDATE).build()
      notificationManager.createNotificationChannel(channel)
      val intent = Intent(applicationContext, AppUpdaterNotificationIntent::class.java)
      notification.contentIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
      val id = UPDATE_AVAILABLE_CODE xor System.currentTimeMillis().toInt()
      notificationManager.notify(id, notification)

      logcat { "AppUpdaterWorker is now no longer doing something" }
      return Result.success()
   }
}

const val CHANNEL_ID_APP_UPDATE = "CHANNEL_ID_APP_UPDATE"
const val UPDATE_AVAILABLE_CODE = 12_345_678
