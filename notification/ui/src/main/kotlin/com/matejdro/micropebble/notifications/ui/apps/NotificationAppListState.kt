package com.matejdro.micropebble.notifications.ui.apps

import io.rebble.libpebblecommon.database.dao.AppWithCount

data class NotificationAppListState(
   val apps: List<AppWithCount>,
   val mutePhoneNotificationSoundsWhenConnected: Boolean,
   val mutePhoneCallSoundsWhenConnected: Boolean,
   val respectDoNotDisturb: Boolean,
   val sendNotifications: Boolean,
)
