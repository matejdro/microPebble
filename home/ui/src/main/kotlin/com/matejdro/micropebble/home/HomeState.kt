package com.matejdro.micropebble.home

import io.rebble.libpebblecommon.connection.KnownPebbleDevice

data class HomeState(
   val pairedDevices: List<KnownPebbleDevice>,
)
