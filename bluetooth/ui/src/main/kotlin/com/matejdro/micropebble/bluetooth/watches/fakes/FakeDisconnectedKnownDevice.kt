package com.matejdro.micropebble.bluetooth.watches.fakes

import io.rebble.libpebblecommon.connection.ConnectionFailureInfo
import io.rebble.libpebblecommon.connection.KnownPebbleDevice
import io.rebble.libpebblecommon.connection.PebbleBleIdentifier
import io.rebble.libpebblecommon.connection.PebbleIdentifier
import io.rebble.libpebblecommon.metadata.WatchColor
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import kotlin.time.Instant

class FakeDisconnectedKnownDevice(
   override val name: String,
   override val nickname: String? = null,
   override val color: WatchColor = WatchColor.Pebble2DuoBlack,
   override val watchType: WatchHardwarePlatform = WatchHardwarePlatform.CORE_ASTERIX,
   override val identifier: PebbleIdentifier = PebbleBleIdentifier(""),
   override val lastConnected: Instant = Instant.DISTANT_PAST,
   override val connectionFailureInfo: ConnectionFailureInfo? = null,
   override val serial: String = "XXXXXXXXXXXX",
) : KnownPebbleDevice {
   override val runningFwVersion: String = "v1.2.3-core"

   override fun forget() {}
   override fun setNickname(nickname: String?) {
   }

   override fun connect() {}
}
