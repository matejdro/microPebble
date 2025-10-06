package com.matejdro.micropebble.apps.ui.developerconnection

data class DeveloperConnectionScreenState(
   val availableWatches: List<Watch> = emptyList(),
   val selectedWatchSerial: String = "",
   val connectionActive: Boolean = false,
   val ip: String = "",
) {
   data class Watch(val title: String, val serial: String)
}
