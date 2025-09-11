package com.matejdro.micropebble.bluetooth.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.matejdro.micropebble.common.di.ServiceKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.ConnectingPebbleDevice
import io.rebble.libpebblecommon.connection.Watches
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Most of the connection code is in the LibPebble3. This service is mainly used to keep the app alive in the background and
 * to show the connecting notification.
 */
@ContributesIntoMap(AppScope::class)
@ServiceKey(WatchConnectionService::class)
@Inject
class WatchConnectionService(
   private val watches: Watches,
) : Service() {
   private val coroutineScope = MainScope()
   override fun onCreate() {
      super.onCreate()

      coroutineScope.launch {
         // Wait a bit until data is loaded from the DB.
         // Workaround for the https://github.com/coredevices/libpebble3/issues/9
         delay(DATABASE_LOAD_WAIT_TIME_MS)

         watches.watches.collect { deviceList ->
            if (deviceList.none { it is ConnectedPebbleDevice || it is ConnectingPebbleDevice }) {
               // Stop the service when we have no watches to connect to
               stopSelf()
            }
         }
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
