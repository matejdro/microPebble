package com.matejdro.micropebble.notifications

interface NotificationsStatus {
   val isServiceRegistered: Boolean

   fun requestNotificationAccess(): Boolean

   val isNotificationAccessEnabled: Boolean
}
