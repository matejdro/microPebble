package com.matejdro.micropebble.onboarding

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.OnboardingKey
import com.matejdro.micropebble.notifications.NotificationsStatus
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.connection.ConnectedPebbleDevice
import io.rebble.libpebblecommon.connection.ConnectingPebbleDevice
import io.rebble.libpebblecommon.connection.Watches
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import kotlin.time.Duration.Companion.milliseconds

@Stable
@Inject
@ContributesScopedService
class OnboardingScreenViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val watches: Watches,
   private val notificationsStatus: NotificationsStatus,
) : SingleScreenViewModel<OnboardingKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<OnboardingState>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<OnboardingState>> = _uiState

   override fun onServiceRegistered() = resources.launchResourceControlTask(_uiState) {
      actionLogger.logAction { "OnboardingScreenViewModel.onServiceRegistered()" }

      emitAll(
         combine(
            watches.watches.map { watches -> watches.any { it is ConnectedPebbleDevice || it is ConnectingPebbleDevice } },
            pollForNotificationListenerPermission()
         ) { anyPairedWatch, hasListenerPermission ->
            Outcome.Success(
               OnboardingState(
                  hasListenerPermission,
                  anyPairedWatch
               )
            )
         }
      )
   }

   fun requestNotificationPermissions() {
      actionLogger.logAction { "MainViewModel.requestNotificationPermissions()" }
      notificationsStatus.requestNotificationAccess()
   }

   private fun pollForNotificationListenerPermission(): Flow<Boolean> = flow {
      while (currentCoroutineContext().isActive) {
         emit(notificationsStatus.isNotificationAccessEnabled)
         delay(500.milliseconds)
      }
   }.distinctUntilChanged()
}

data class OnboardingState(
   val hasNotificationListenerPermission: Boolean,
   val anyWatchPaired: Boolean,
)
