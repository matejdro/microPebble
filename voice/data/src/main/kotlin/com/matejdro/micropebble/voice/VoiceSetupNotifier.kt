package com.matejdro.micropebble.voice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.matejdro.micropebble.voice.data.R
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import com.matejdro.micropebble.sharedresources.R as sharedR

@Inject
@SingleIn(AppScope::class)
class VoiceSetupNotifier(
   private val context: Context,
) {
   fun showNoDefaultVoiceServiceNotification() {
      ensureChannel()
      val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
         addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      val pendingIntent = PendingIntent.getActivity(
         context,
         REQUEST_CODE_OPEN_VOICE_SETTINGS,
         intent,
         PendingIntent.FLAG_IMMUTABLE,
      )
      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
         .setSmallIcon(sharedR.drawable.ic_mic)
         .setContentTitle(context.getString(R.string.voice_setup_needed_title))
         .setContentText(context.getString(R.string.voice_setup_needed_text))
         .setContentIntent(pendingIntent)
         .setAutoCancel(true)
         .setPriority(NotificationCompat.PRIORITY_DEFAULT)
         .build()
      NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
   }

   private fun ensureChannel() {
      val channel = NotificationChannel(
         CHANNEL_ID,
         context.getString(R.string.voice_setup_channel_name),
         NotificationManager.IMPORTANCE_DEFAULT,
      )
      context.getSystemService<NotificationManager>()!!.createNotificationChannel(channel)
   }
}

private const val CHANNEL_ID = "voice_setup_channel_id"
private const val NOTIFICATION_ID = 12379
private const val REQUEST_CODE_OPEN_VOICE_SETTINGS = 0
