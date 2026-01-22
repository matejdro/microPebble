package com.matejdro.micropebble.appstore.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.binding
import si.inova.kotlinova.core.logging.logcat

@AssistedInject
@IntoMap
class AppUpdaterWorker(
   @Assisted
   context: Context,
   @Assisted
   params: WorkerParameters,
) : CoroutineWorker(context, params) {
   override suspend fun doWork(): Result {
      logcat { "AppUpdaterWorker is now doing something" }
      val notificationManager = applicationContext.getSystemService<NotificationManager>()!!
      val channel = NotificationChannel(CHANNEL_ID_APP_UPDATE, "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
      notificationManager.createNotificationChannel(channel)
      val intent = PendingIntent.getActivity(
         /* context = */ applicationContext,
         /* requestCode = */ 0,
         /* intent = */ Intent(Intent.ACTION_VIEW, "micropebble://watchapps".toUri()),
         /* flags = */ PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )
      val notification = Notification.Builder(applicationContext, CHANNEL_ID_APP_UPDATE).run {
         setContentIntent(intent)
         setSmallIcon(R.drawable.ic_update)
         setContentTitle("Hi")
         setContentText("App is being updated :)")
         build()
      }
      val id = UPDATE_AVAILABLE_CODE xor System.currentTimeMillis().toInt()

      // make not lambda to enable notifications every 15 minutes
      ({ notificationManager.notify(id, notification) })

      logcat { "AppUpdaterWorker is now no longer doing something" }
      return Result.success()
   }

   @AssistedFactory
   @IntoMap
   @ContributesIntoMap(AppScope::class, binding<(Context, WorkerParameters) -> ListenableWorker>())
   @WorkerKey(AppUpdaterWorker::class)
   interface Factory : (Context, WorkerParameters) -> ListenableWorker {
      fun create(context: Context, params: WorkerParameters): AppUpdaterWorker

      override fun invoke(p1: Context, p2: WorkerParameters) = create(p1, p2)
   }
}

const val CHANNEL_ID_APP_UPDATE = "CHANNEL_ID_APP_UPDATE"
const val UPDATE_AVAILABLE_CODE = 12_345_678
