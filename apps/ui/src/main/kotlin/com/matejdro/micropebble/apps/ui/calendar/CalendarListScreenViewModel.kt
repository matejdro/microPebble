package com.matejdro.micropebble.apps.ui.calendar

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.CalendarListScreenKey
import dev.zacsweers.metro.Inject
import dispatch.core.flowOnDefault
import io.rebble.libpebblecommon.connection.Calendar
import io.rebble.libpebblecommon.database.entity.CalendarEntity
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
class CalendarListScreenViewModel(
   private val resources: CoroutineResourceManager,
   private val calendar: Calendar,
   private val actionLogger: ActionLogger,
) : SingleScreenViewModel<CalendarListScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<List<CalendarOwner>>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<List<CalendarOwner>>> = _uiState

   override fun onServiceRegistered() {
      actionLogger.logAction { "CalendarListScreenViewModel.onServiceRegistered()" }
      resources.launchResourceControlTask(_uiState) {
         val flow = calendar.calendars()
            .map { calendars ->
               Outcome.Success(
                  calendars.groupBy { it.ownerName }
                     .entries.map {
                        CalendarOwner(
                           it.key,
                           it.value
                        )
                     }
               )
            }
            .flowOnDefault()

         emitAll(flow)
      }
   }

   fun setCalendarEnabled(id: Int, enabled: Boolean) = resources.launchWithExceptionReporting {
      actionLogger.logAction { "CalendarListScreenViewModel.setCalendarEnabled($id, $enabled)" }
      calendar.updateCalendarEnabled(id, enabled)
   }
}

data class CalendarOwner(
   val title: String,
   val calendars: List<CalendarEntity>,
)
