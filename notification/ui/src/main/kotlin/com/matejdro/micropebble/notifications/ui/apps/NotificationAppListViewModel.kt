package com.matejdro.micropebble.notifications.ui.apps

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.NotificationAppListKey
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.LibPebble
import io.rebble.libpebblecommon.connection.NotificationApps
import io.rebble.libpebblecommon.database.entity.MuteState
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
class NotificationAppListViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val notificationApps: NotificationApps,
   private val libPebble: LibPebble,
) : SingleScreenViewModel<NotificationAppListKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<NotificationAppListState>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<NotificationAppListState>> = _uiState

   override fun onServiceRegistered() {
      actionLogger.logAction { "NotificationAppListViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_uiState) {
         val configFlow = libPebble.config
         val appsFlow = notificationApps.notificationApps()

         val combinedFlow = combine(appsFlow, configFlow) { apps, config ->
            val notificationsConfig = config.notificationConfig
            val watchConfig = config.watchConfig

            Outcome.Success(
               NotificationAppListState(
                  apps,
                  notificationsConfig.mutePhoneNotificationSoundsWhenConnected,
                  notificationsConfig.mutePhoneCallSoundsWhenConnected,
                  notificationsConfig.respectDoNotDisturb,
                  notificationsConfig.sendNotifications,
                  notificationsConfig.useAndroidVibePatterns,
                  watchConfig.calendarReminders,
               )
            )
         }

         emitAll(combinedFlow)
      }
   }

   fun setAppEnabled(packageName: String, enabled: Boolean) {
      actionLogger.logAction { "NotificationAppListViewModel.toggleAppEnabled(packageName = $packageName, enabled = $enabled)" }

      notificationApps.updateNotificationAppMuteState(packageName, if (enabled) MuteState.Never else MuteState.Always)
   }

   fun setNotificationsPhoneMute(mute: Boolean) {
      actionLogger.logAction { "NotificationAppListViewModel.toggleNotificationsPhoneMute()" }

      libPebble.updateConfig(
         libPebble.config.value.copy(
            notificationConfig = libPebble.config.value.notificationConfig.copy(
               mutePhoneNotificationSoundsWhenConnected = mute
            )
         )
      )
   }

   fun setCallsPhoneMute(mute: Boolean) {
      actionLogger.logAction { "NotificationAppListViewModel.toggleNotificationsPhoneMute()" }

      libPebble.updateConfig(
         libPebble.config.value.copy(
            notificationConfig = libPebble.config.value.notificationConfig.copy(
               mutePhoneCallSoundsWhenConnected = mute
            )
         )
      )
   }

   fun setRespectDoNotDisturb(respect: Boolean) {
      actionLogger.logAction { "NotificationAppListViewModel.setRespectDoNotDisturb(respect = $respect)" }

      libPebble.updateConfig(
         libPebble.config.value.copy(
            notificationConfig = libPebble.config.value.notificationConfig.copy(
               respectDoNotDisturb = respect
            )
         )
      )
   }

   fun setSendNotifications(send: Boolean) {
      actionLogger.logAction { "NotificationAppListViewModel.setSendNotifications(send = $send)" }

      libPebble.updateConfig(
         libPebble.config.value.copy(
            notificationConfig = libPebble.config.value.notificationConfig.copy(
               sendNotifications = send
            )
         )
      )
   }

   fun setSendCalendarReminders(send: Boolean) {
      actionLogger.logAction { "NotificationAppListViewModel.setSendCalendarReminders(send = $send)" }

      libPebble.updateConfig(
         libPebble.config.value.copy(
            watchConfig = libPebble.config.value.watchConfig.copy(
               calendarReminders = send
            )
         )
      )
   }

   fun setUseAndroidVibrationPatterns(send: Boolean) {
      actionLogger.logAction { "NotificationAppListViewModel.setUseAndroidVibrationPatterns(send = $send)" }

      libPebble.updateConfig(
         libPebble.config.value.copy(
            notificationConfig = libPebble.config.value.notificationConfig.copy(
               useAndroidVibePatterns = send
            )
         )
      )
   }
}
