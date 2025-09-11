package com.matejdro.micropebble.bluetooth.service

import android.content.Context
import android.content.Intent
import com.matejdro.micropebble.bluetooth.api.ConnectionServiceStarter
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class ConnectionServiceStarterImpl(private val context: Context) : ConnectionServiceStarter {
   override fun start() {
      context.startService(Intent(context, WatchConnectionService::class.java))
   }
}
