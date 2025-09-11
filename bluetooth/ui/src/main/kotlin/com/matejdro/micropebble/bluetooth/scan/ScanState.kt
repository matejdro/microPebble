package com.matejdro.micropebble.bluetooth.scan

import io.rebble.libpebblecommon.connection.PebbleDevice

data class ScanState(
   val bluetoothOn: Boolean,
   val scanning: Boolean,
   val foundDevices: List<PebbleDevice>,
)
