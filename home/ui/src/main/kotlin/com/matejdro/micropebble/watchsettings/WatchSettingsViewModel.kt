package com.matejdro.micropebble.watchsettings

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.WatchSettingsScreenKey
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.WatchPrefs
import io.rebble.libpebblecommon.database.dao.WatchPreference
import io.rebble.libpebblecommon.database.entity.RgbColorWatchPref
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class WatchSettingsViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val watchPrefs: WatchPrefs,
) : SingleScreenViewModel<WatchSettingsScreenKey>(resources.scope) {

   private val _state = MutableStateFlow<Outcome<WatchSettingsState>>(Outcome.Progress())
   val state: StateFlow<Outcome<WatchSettingsState>> = _state

   override fun onServiceRegistered() {
      actionLogger.logAction { "WatchSettingsViewModel.onServiceRegistered()" }
      resources.launchResourceControlTask(_state) {
         emitAll(
            watchPrefs.watchPrefs.map { prefs ->
               @Suppress("UNCHECKED_CAST")
               val pref = prefs.firstOrNull { it.pref == RgbColorWatchPref.BacklightColor }
                  as? WatchPreference<UInt>
               Outcome.Success(
                  WatchSettingsState(
                     backlightColor = pref?.valueOrDefault() ?: RgbColorWatchPref.BacklightColor.defaultValue,
                  )
               )
            }
         )
      }
   }

   fun setBacklightColor(rgb: UInt) {
      actionLogger.logAction { "WatchSettingsViewModel.setBacklightColor($rgb)" }
      watchPrefs.setWatchPref(WatchPreference(RgbColorWatchPref.BacklightColor, rgb))
   }
}

data class WatchSettingsState(
   val backlightColor: UInt = RgbColorWatchPref.BacklightColor.defaultValue,
)
