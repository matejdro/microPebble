package com.matejdro.micropebble.notifications

import android.companion.CompanionDeviceManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.notification.LibPebbleNotificationListener

@Inject
@ContributesBinding(AppScope::class)
class NotificationsStatusImpl(
   private val context: Context,
) : NotificationsStatus {
   override val isServiceRegistered: Boolean
      get() {
         return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
      }

   override fun requestNotificationAccess(): Boolean {
      val companionManager = context.getSystemService<CompanionDeviceManager>() ?: return false
      val associations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
         companionManager.myAssociations
      } else {
         @Suppress("DEPRECATION")
         companionManager.associations
      }

      if (associations.isEmpty()) {
         return false
      }

      companionManager.requestNotificationAccess(ComponentName(context, LibPebbleNotificationListener::class.java))
      return true
   }
}
