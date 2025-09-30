package com.matejdro.micropebble.bluetooth

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import io.rebble.libpebblecommon.connection.LibPebble
import io.rebble.libpebblecommon.connection.LockerApi
import io.rebble.libpebblecommon.connection.NotificationApps
import io.rebble.libpebblecommon.connection.Scanning
import io.rebble.libpebblecommon.connection.Watches

@ContributesTo(AppScope::class)
interface LibPebbleBindings {
   @Binds
   fun bindToScanning(
      libPebble: LibPebble,
   ): Scanning

   @Binds
   fun bindToWatches(
      libPebble: LibPebble,
   ): Watches

   @Binds
   fun bindToNotificationApps(
      libPebble: LibPebble,
   ): NotificationApps

   @Binds
   fun bindToLockerApi(
      libPebble: LibPebble,
   ): LockerApi
}
