package com.matejdro.micropebble.notifications

import androidx.compose.runtime.Stable

@Stable
interface NotificationsStatus {
   val isServiceRegistered: Boolean

   fun requestNotificationAccess(): Boolean

   val isNotificationAccessEnabled: Boolean
}
