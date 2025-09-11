package com.matejdro.micropebble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matejdro.micropebble.navigation.keys.base.HomeScreenKey
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

class MainViewModel @Inject constructor() : ViewModel() {
   private val _startingScreen = MutableStateFlow<ScreenKey?>(null)
   val startingScreen: StateFlow<ScreenKey?> = _startingScreen

   init {
      viewModelScope.launch {
         _startingScreen.value = HomeScreenKey
      }
   }

   @AssistedFactory
   fun interface Factory {
      fun create(): MainViewModel
   }
}
