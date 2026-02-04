package com.matejdro.micropebble.appstore.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.sharedresources.R
import io.rebble.libpebblecommon.metadata.WatchType

fun String.getIcon() = when (this) {
   WatchType.APLITE.codename -> R.drawable.ic_hardware_aplite
   WatchType.BASALT.codename -> R.drawable.ic_hardware_basalt
   WatchType.CHALK.codename -> R.drawable.ic_hardware_chalk
   WatchType.DIORITE.codename -> R.drawable.ic_hardware_diorite
   WatchType.EMERY.codename -> R.drawable.ic_hardware_emery
   WatchType.FLINT.codename -> R.drawable.ic_hardware_flint
   "gabbro" -> R.drawable.ic_hardware_gabbro
   else -> null
}

fun WatchType.getIcon() = codename.getIcon()!!

@Composable
fun getWatchesForCodename(codename: String): List<String> =
   when (codename) {
      WatchType.APLITE.codename -> listOf(R.string.watch_classic, R.string.watch_classic_steel)
      WatchType.BASALT.codename -> listOf(R.string.watch_time, R.string.watch_time_steel)
      WatchType.CHALK.codename -> listOf(R.string.watch_time_round)
      WatchType.DIORITE.codename -> listOf(R.string.watch_pebble_2)
      WatchType.EMERY.codename -> listOf(R.string.watch_pebble_time_2)
      WatchType.FLINT.codename -> listOf(R.string.watch_pebble_2_duo)
      "gabbro" -> listOf(R.string.watch_round_2)
      else -> listOf(R.string.watch_unknown)
   }.map { stringResource(it) }

fun Application.isUnofficiallyCompatibleWith(watchType: WatchType?) =
   if (watchType == null) {
      true
   } else {
      watchType.getBestVariant((compatibility - "android" - "ios").filter { (_, info) -> info.supported }.keys.toList()) != null
   }

fun Application.isCompatibleWith(type: WatchType) = compatibility[type.codename]?.supported == true

fun WatchType.isCircular() = when (this.codename) {
   "chalk" -> true
   "gabbro" -> true
   else -> false
}
