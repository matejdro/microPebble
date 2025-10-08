package com.matejdro.micropebble.bluetooth.watches

import io.rebble.libpebblecommon.connection.KnownPebbleDevice

data class WatchListState(
   val pairedDevices: List<KnownPebbleDevice>,
)
