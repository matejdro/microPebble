package com.matejdro.micropebble.bluetooth.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.matejdro.micropebble.common.di.ServiceKey
import com.matejdro.micropebble.common.notifications.MainActivityProvider
import com.matejdro.micropebble.common.notifications.NotificationKeys
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.CommonConnectedDevice
import io.rebble.libpebblecommon.connection.ConnectingPebbleDevice
import io.rebble.libpebblecommon.connection.Watches
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.matejdro.micropebble.sharedresources.R as sharedR

/**
 * Most of the connection code is in the LibPebble3. This service is mainly used to keep the app alive in the background and
 * to show the connecting notification.
 */
@ContributesIntoMap(AppScope::class)
@ServiceKey(WatchConnectionService::class)
@Inject
class WatchConnectionService(
   private val watches: Watches,
   private val mainActivityProvider: MainActivityProvider,
) : Service() {
   private val coroutineScope = MainScope()
   private var finishTimer: Job? = null
   override fun onCreate() {
      super.onCreate()

      coroutineScope.launch {
         watches.watches.collect { deviceList ->
            if (deviceList.isEmpty()) {
               startFinishTimer()
            } else {
               finishTimer?.cancel()
            }

            if (deviceList.none { it is CommonConnectedDevice || it is ConnectingPebbleDevice }) {
               // Stop the service when we have no watches to connect to
               stopSelf()
            } else if (deviceList.any { it is ConnectingPebbleDevice }) {
               startForeground(
                  NotificationKeys.ID_FOREGROUND_CONNECTING,
                  Notification.Builder(this@WatchConnectionService, NotificationKeys.CHANNEL_CONNECTING)
                     .setContentTitle(getString(sharedR.string.app_name))
                     .setContentText(getString(sharedR.string.connecting))
                     .setSmallIcon(sharedR.drawable.ic_notification)
                     .setContentIntent(
                        PendingIntent.getActivity(
                           this@WatchConnectionService,
                           0,
                           mainActivityProvider.getMainActivityIntent(),
                           PendingIntent.FLAG_IMMUTABLE
                        )
                     )
                     .build()
               )
            } else {
               stopForeground(STOP_FOREGROUND_REMOVE)
            }
         }
      }
   }

   private fun startFinishTimer() {
      // Wait a bit until data is loaded from the DB.
      // Workaround for the https://github.com/coredevices/libpebble3/issues/9

      finishTimer = coroutineScope.launch {
         delay(DATABASE_LOAD_WAIT_TIME_MS)
         stopSelf()
      }
   }

   override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
      return START_STICKY
   }

   override fun onBind(intent: Intent?): IBinder? {
      return null
   }
}

private const val DATABASE_LOAD_WAIT_TIME_MS = 5_000L
