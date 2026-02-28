package com.matejdro.micropebble.voice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.matejdro.micropebble.common.di.ServiceKey
import com.matejdro.micropebble.voice.data.R
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import logcat.logcat
import com.matejdro.micropebble.sharedresources.R as sharedR

/**
 * A dummy service that does nothing but run in foreground.
 * This is required for having background microphone permission in Android 11+
 */
@ContributesIntoMap(AppScope::class)
@ServiceKey(VoicePermissionService::class)
@Inject
class VoicePermissionService(
   private val serviceController: VoicePermissionServiceControlImpl,
) : Service() {
   override fun onCreate() {
      super.onCreate()

      logcat { "Voice service created" }
      createChannel()

      startForeground(
         NOTIFICATION_ID,
         NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(sharedR.drawable.ic_mic)
            .setContentTitle(getString(R.string.voice_recognition))
            .setContentText(getString(R.string.voice_recognition_description))
            .build(),
         ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
      )

      logcat { "Foreground started" }
      serviceController.voiceServiceActive.value = true
   }

   override fun onDestroy() {
      logcat { "Foreground destroyed" }
      serviceController.voiceServiceActive.value = false
      super.onDestroy()
   }

   override fun onBind(intent: Intent?): IBinder? {
      return null
   }

   private fun createChannel() {
      val channel = NotificationChannel(
         CHANNEL_ID,
         getString(R.string.voice_recognition),
         NotificationManager.IMPORTANCE_DEFAULT
      )
      val manager = getSystemService(NotificationManager::class.java)
      manager.createNotificationChannel(channel)
   }
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class VoicePermissionServiceControlImpl(
   private val context: Context,
) : VoicePermissionServiceControl {
   override val voiceServiceActive = MutableStateFlow<Boolean>(false)

   override fun start() {
      context.startForegroundService(Intent(context, VoicePermissionService::class.java))
   }
}

private const val CHANNEL_ID = "voice_channel_id"
private const val NOTIFICATION_ID = 12378
