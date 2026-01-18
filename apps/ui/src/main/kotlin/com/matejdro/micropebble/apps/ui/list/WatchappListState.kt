package com.matejdro.micropebble.apps.ui.list

import io.rebble.libpebblecommon.locker.LockerWrapper
import kotlin.uuid.Uuid

data class WatchappListState(
   val watchfaces: List<LockerWrapper>,
   val watchapps: List<LockerWrapper>,
   val updatableWatchfaces: List<Uuid> = emptyList(),
)
