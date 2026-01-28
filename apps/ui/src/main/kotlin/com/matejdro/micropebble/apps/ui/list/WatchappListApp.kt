package com.matejdro.micropebble.apps.ui.list

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.appstore.api.AppStatus
import io.rebble.libpebblecommon.locker.LockerWrapper

@Stable
data class WatchappListApp(
   val app: LockerWrapper,
   val appStatus: AppStatus,
)
