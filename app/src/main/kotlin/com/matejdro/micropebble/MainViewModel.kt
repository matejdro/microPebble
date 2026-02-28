package com.matejdro.micropebble

import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.HomeScreenKey
import com.matejdro.micropebble.navigation.keys.OnboardingKey
import com.matejdro.micropebble.navigation.keys.WatchListKey
import com.matejdro.micropebble.pbw.FileInstallHandler
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dispatch.core.MainImmediateCoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.instructions.OpenScreen
import si.inova.kotlinova.navigation.instructions.ReplaceBackstack
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@AssistedInject
class MainViewModel(
   private val actionLogger: ActionLogger,
   private val preferences: DataStore<Preferences>,
   mainScope: MainImmediateCoroutineScope,
   private val fileInstallHandler: FileInstallHandler,
   @Assisted
   private val intent: Intent,
) : ViewModel(mainScope) {

   private val _startingScreens = MutableStateFlow<List<ScreenKey>?>(null)
   val startingScreens: StateFlow<List<ScreenKey>?> = _startingScreens

   private val _navigationTarget = Channel<NavigationInstruction>(Channel.BUFFERED)
   val navigationTarget: Flow<NavigationInstruction> = _navigationTarget.receiveAsFlow()

   init {
      actionLogger.logAction { "MainViewModel init()" }
      viewModelScope.launch {
         _startingScreens.value = if (preferences.data.first()[onboardingShown] == true) {
            val installScreens = fileInstallHandler.getTargetScreens(intent)
            if (installScreens.firstOrNull() is HomeScreenKey) {
               installScreens
            } else {
               listOf(HomeScreenKey, WatchListKey) + installScreens
            }
         } else {
            listOf(OnboardingKey)
         }
         preferences.edit {
            it[onboardingShown] = true
         }
      }
   }

   fun onNewIntent(intent: Intent) = viewModelScope.launch {
      actionLogger.logAction { "MainViewModel.onNewIntent($intent)" }
      val installScreens = fileInstallHandler.getTargetScreens(intent)
      if (installScreens.isEmpty()) {
         return@launch
      }
      _navigationTarget.send(
         if (installScreens.firstOrNull() is HomeScreenKey) {
            ReplaceBackstack(*installScreens.toTypedArray())
         } else {
            OpenScreen(installScreens.first())
         }
      )
   }

   @AssistedFactory
   fun interface Factory {
      fun create(intent: Intent): MainViewModel
   }
}

private val onboardingShown = booleanPreferencesKey("onboarding_shown")
