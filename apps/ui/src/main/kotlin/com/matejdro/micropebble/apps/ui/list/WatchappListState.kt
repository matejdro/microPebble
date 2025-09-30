package com.matejdro.micropebble.apps.ui.list

import io.rebble.libpebblecommon.locker.AppBasicProperties

data class WatchappListState(
   val apps: List<AppBasicProperties>,
)
