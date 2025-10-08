package com.matejdro.micropebble.tools

import android.content.Context
import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class ToolsViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val context: Context,
) : SingleScreenViewModel<ToolsScreenKey>(resources.scope) {
   private val _appVersion = MutableStateFlow<String>("")
   val appVersion: StateFlow<String>
      get() = _appVersion

   override fun onServiceRegistered() {
      actionLogger.logAction { "ToolsViewModel.onServiceRegistered()" }

      val pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
      _appVersion.value = pInfo.versionName.orEmpty()
   }
}
