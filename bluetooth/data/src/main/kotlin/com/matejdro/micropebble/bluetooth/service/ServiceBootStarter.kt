package com.matejdro.micropebble.bluetooth.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.matejdro.micropebble.bluetooth.api.ConnectionServiceStarter
import com.matejdro.micropebble.common.di.ReceiverKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject

@Inject
@ContributesIntoMap(AppScope::class)
@ReceiverKey(ServiceBootStarter::class)
class ServiceBootStarter(
   private val serviceStarter: ConnectionServiceStarter,
) : BroadcastReceiver() {
   override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
         serviceStarter.start()
      }
   }
}
