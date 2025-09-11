package com.matejdro.micropebble.home.fakes

import io.rebble.libpebblecommon.connection.ConnectingKnownPebbleDevice
import io.rebble.libpebblecommon.connection.PebbleBleIdentifier
import io.rebble.libpebblecommon.connection.PebbleIdentifier
import io.rebble.libpebblecommon.metadata.WatchColor
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class FakeKnownConnectingDevice(
   override val name: String,
   override val nickname: String? = null,
   override val color: WatchColor = WatchColor.Pebble2DuoBlack,
   override val watchType: WatchHardwarePlatform = WatchHardwarePlatform.CORE_ASTERIX,
   override val identifier: PebbleIdentifier = PebbleBleIdentifier(""),
   override val lastConnected: Instant = Instant.DISTANT_PAST,
   override val negotiating: Boolean = false,
   override val rebootingAfterFirmwareUpdate: Boolean = false,
) : ConnectingKnownPebbleDevice {
   override val runningFwVersion: String = "v1.2.3-core"
   override val serial: String = "XXXXXXXXXXXX"

   override fun forget() {}
   override fun setNickname(nickname: String?) {
   }

   override fun connect() {}

   override fun disconnect() {}
}
