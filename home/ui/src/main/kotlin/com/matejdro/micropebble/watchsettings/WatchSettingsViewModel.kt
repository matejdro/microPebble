package com.matejdro.micropebble.watchsettings

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.WatchSettingsScreenKey
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.LibPebble
import io.rebble.libpebblecommon.connection.Vibrations
import io.rebble.libpebblecommon.connection.WatchPrefs
import io.rebble.libpebblecommon.database.dao.WatchPreference
import io.rebble.libpebblecommon.database.entity.RgbColorWatchPref
import io.rebble.libpebblecommon.notification.VibePattern
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
   private val vibrations: Vibrations,
   private val libPebble: LibPebble,
) : SingleScreenViewModel<WatchSettingsScreenKey>(resources.scope) {

   private val _state = MutableStateFlow<Outcome<WatchSettingsState>>(Outcome.Progress())
   val state: StateFlow<Outcome<WatchSettingsState>> = _state

   override fun onServiceRegistered() {
      actionLogger.logAction { "WatchSettingsViewModel.onServiceRegistered()" }
      resources.launchResourceControlTask(_state) {
         emitAll(
            combine(
               watchPrefs.watchPrefs,
               vibrations.vibePatterns(),
               libPebble.config,
            ) { prefs, vibePatterns, config ->
               @Suppress("UNCHECKED_CAST")
               val backlightPref = prefs.firstOrNull { it.pref == RgbColorWatchPref.BacklightColor }
                  as? WatchPreference<UInt>
               Outcome.Success(
                  WatchSettingsState(
                     backlightColor = backlightPref?.valueOrDefault() ?: RgbColorWatchPref.BacklightColor.defaultValue,
                     customVibePatterns = vibePatterns.filter { !it.bundled },
                     overrideNotificationVibePattern = config.notificationConfig.overrideDefaultVibePattern,
                     overrideCalendarVibePattern = config.notificationConfig.overrideCalendarVibePattern,
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

   @Suppress("NullableToStringCall") // patternName=null is meaningful in the log (means "no override")
   fun setOverrideNotificationVibePattern(patternName: String?) {
      actionLogger.logAction { "WatchSettingsViewModel.setOverrideNotificationVibePattern($patternName)" }
      val current = libPebble.config.value
      libPebble.updateConfig(
         current.copy(
            notificationConfig = current.notificationConfig.copy(
               overrideDefaultVibePattern = patternName,
            ),
         ),
      )
   }

   @Suppress("NullableToStringCall") // patternName=null is meaningful in the log (means "no override")
   fun setOverrideCalendarVibePattern(patternName: String?) {
      actionLogger.logAction { "WatchSettingsViewModel.setOverrideCalendarVibePattern($patternName)" }
      val current = libPebble.config.value
      libPebble.updateConfig(
         current.copy(
            notificationConfig = current.notificationConfig.copy(
               overrideCalendarVibePattern = patternName,
            ),
         ),
      )
   }
}

data class WatchSettingsState(
   val backlightColor: UInt = RgbColorWatchPref.BacklightColor.defaultValue,
   val customVibePatterns: List<VibePattern> = emptyList(),
   val overrideNotificationVibePattern: String? = null,
   val overrideCalendarVibePattern: String? = null,
)
