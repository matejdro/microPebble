package com.matejdro.micropebble.apps.ui.list

import io.rebble.libpebblecommon.locker.LockerWrapper

data class WatchappListState(
   val watchfaces: List<LockerWrapper>,
   val watchapps: List<LockerWrapper>,
)
