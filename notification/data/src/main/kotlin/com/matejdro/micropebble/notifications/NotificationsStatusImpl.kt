package com.matejdro.micropebble.notifications

import android.app.NotificationManager
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
   private val companionManager = requireNotNull(context.getSystemService<CompanionDeviceManager>())
   private val notificationManager = requireNotNull(context.getSystemService<NotificationManager>())

   override val isServiceRegistered: Boolean
      get() {
         return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
      }

   override fun requestNotificationAccess(): Boolean {
      @Suppress("DEPRECATION")
      val associations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
         companionManager.myAssociations
      } else {
         companionManager.associations
      }

      if (associations.isEmpty()) {
         return false
      }

      companionManager.requestNotificationAccess(getNotificationListenerComponent())
      return true
   }

   override val isNotificationAccessEnabled: Boolean
      get() = notificationManager.isNotificationListenerAccessGranted(getNotificationListenerComponent())

   private fun getNotificationListenerComponent(): ComponentName =
      ComponentName(context, LibPebbleNotificationListener::class.java)
}
