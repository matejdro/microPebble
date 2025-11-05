package com.matejdro.micropebble.apps.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.matejdro.micropebble.navigation.keys.CalendarListScreenKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import io.rebble.libpebblecommon.database.entity.CalendarEntity
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class CalendarListScreen(
   private val viewModel: CalendarListScreenViewModel,
) : Screen<CalendarListScreenKey>() {
   @Composable
   override fun Content(key: CalendarListScreenKey) {
      val stateOutcome = viewModel.uiState.collectAsStateWithLifecycleAndBlinkingPrevention().value

      ProgressErrorSuccessScaffold(
         stateOutcome,
         Modifier
            .fillMaxSize()
            .safeDrawingPadding()
      ) { state ->
         CalendarListScreenContent(state, viewModel::setCalendarEnabled)
      }
   }
}

@Composable
private fun CalendarListScreenContent(calendarsOwners: List<CalendarOwner>, setCalendarEnabled: (Int, Boolean) -> Unit) {
   Surface(Modifier.fillMaxSize()) {
      LazyColumn(
         Modifier.fillMaxSize(),
         contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
         verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
         for (owner in calendarsOwners) {
            item(key = "header-${owner.title}") {
               Text(owner.title, Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp))
            }

            for (calendar in owner.calendars) {
               item(key = calendar.id) {
                  Row(
                     Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, end = 16.dp)
                        .clickable(onClick = { setCalendarEnabled(calendar.id, !calendar.enabled) }),
                     verticalAlignment = Alignment.CenterVertically
                  ) {
                     val shape = RoundedCornerShape(8.dp)
                     Box(
                        Modifier
                           .padding(end = 16.dp)
                           .size(24.dp)
                           .background(Color(calendar.color), shape = shape)
                           .border(Dp.Hairline, MaterialTheme.colorScheme.onSurface, shape)
                     )

                     Text(
                        calendar.name,
                        Modifier
                           .padding(end = 16.dp)
                           .weight(1f)
                     )
                     Switch(calendar.enabled, onCheckedChange = { setCalendarEnabled(calendar.id, it) })
                  }
               }
            }
         }
      }
   }
}

@FullScreenPreviews
@Composable
internal fun CalendarListScreenPreview() {
   PreviewTheme {
      CalendarListScreenContent(
         calendarsOwners = listOf(
            CalendarOwner(
               "Account 1",
               listOf(
                  CalendarEntity(
                     1,
                     "",
                     "Calendar A",
                     "",
                     "",
                     android.graphics.Color.RED,
                     true
                  ),
                  CalendarEntity(
                     2,
                     "",
                     "Calendar B",
                     "",
                     "",
                     android.graphics.Color.BLACK,
                     false
                  ),
                  CalendarEntity(
                     3,
                     "",
                     "Calendar C",
                     "",
                     "",
                     android.graphics.Color.WHITE,
                     false
                  ),
               )
            ),
            CalendarOwner(
               "Account 2",
               listOf(
                  CalendarEntity(
                     4,
                     "",
                     "Calendar D",
                     "",
                     "",
                     android.graphics.Color.YELLOW,
                     false
                  ),
                  CalendarEntity(
                     5,
                     "",
                     "Calendar E",
                     "",
                     "",
                     android.graphics.Color.BLUE,
                     true
                  ),
               )
            )
         ),
         { _, _ -> }
      )
   }
}
