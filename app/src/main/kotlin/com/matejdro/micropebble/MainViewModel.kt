package com.matejdro.micropebble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.base.HomeScreenKey
import com.matejdro.micropebble.notifications.NotificationsStatus
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

class MainViewModel @Inject constructor(
   private val actionLogger: ActionLogger,
   private val notificationsStatus: NotificationsStatus,
) : ViewModel() {

   private val _startingScreen = MutableStateFlow<ScreenKey?>(null)
   val startingScreen: StateFlow<ScreenKey?> = _startingScreen
   private val _mainState = MutableStateFlow<MainState?>(null)

   val mainState: StateFlow<MainState?>
      get() = _mainState
   init {
      viewModelScope.launch {
         _startingScreen.value = HomeScreenKey

         _mainState.value = MainState(
            notificationsStatus.isServiceRegistered
         )
      }
   }

   fun requestNotificationPermissions() {
      actionLogger.logAction { "MainViewModel.requestNotificationPermissions()" }
      notificationsStatus.requestNotificationAccess()
   }

   @AssistedFactory
   fun interface Factory {
      fun create(): MainViewModel
   }
}
