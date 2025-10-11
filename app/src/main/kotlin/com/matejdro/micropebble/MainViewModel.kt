package com.matejdro.micropebble

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
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

class MainViewModel @Inject constructor(
   private val actionLogger: ActionLogger,
   private val preferences: DataStore<Preferences>,
) : ViewModel() {

   private val _startingScreen = MutableStateFlow<ScreenKey?>(null)
   val startingScreen: StateFlow<ScreenKey?> = _startingScreen

   init {
      actionLogger.logAction { "MainViewModel init()" }
      viewModelScope.launch {
         _startingScreen.value = if (preferences.data.first()[onboardingShown] == true) {
            HomeScreenKey(WatchListKey)
         } else {
            OnboardingKey
         }
         preferences.edit {
            it[onboardingShown] = true
         }
      }
   }

   @AssistedFactory
   fun interface Factory {
      fun create(): MainViewModel
   }
}

private val onboardingShown = booleanPreferencesKey("onboarding_shown")
