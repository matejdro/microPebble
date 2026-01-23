package com.matejdro.micropebble.ui.lists

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.getSystemService
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import kotlinx.coroutines.launch

private const val TICK_DURATION: Long = 30
private const val CLICK_DURATION: Long = 50

@Composable
fun <T> ReorderableListContainer(
   data: List<T>,
   lazyListState: LazyListState,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   content: @Composable ReorderableListScope<T>.(List<T>) -> Unit,
) {
   val reorderState = rememberReorderState<T>(dragAfterLongPress = true)

   var reorderingList by remember(data) { mutableStateOf(data) }
   var dragging by remember { mutableStateOf(false) }
   val density = LocalDensity.current
   val vibrator = LocalContext.current.getSystemService<Vibrator>()
   val coroutineScope = rememberCoroutineScope()

   var lastDragIndex by remember { mutableIntStateOf(-1) }

   val scope = object : ReorderableListScope<T> {
      @Composable
      override fun ReorderableListItem(
         key: Any,
         data: T,
         setOrder: (toIndex: Int) -> Unit,
         modifier: Modifier,
         content: @Composable (Modifier) -> Unit,
      ) {
         ReorderableItem(
            state = reorderState,
            key = key,
            data = data,
            onDragEnter = { state ->
               reorderingList = reorderingList.toMutableList().apply {
                  val index = indexOf(data)
                  if (index == -1) return@ReorderableItem
                  remove(state.data)
                  add(index, state.data)

                  if (lastDragIndex != index) {
                     // Sometimes onDragEnter is called twice. Wrap in this check to ensure we don't vibrate twice
                     vibrator?.vibrate(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                           VibrationEffect.createPredefined(
                              if (dragging) {
                                 VibrationEffect.EFFECT_TICK
                              } else {
                                 VibrationEffect.EFFECT_CLICK
                              }
                           )
                        } else {
                           if (dragging) {
                              VibrationEffect.createOneShot(TICK_DURATION, VibrationEffect.DEFAULT_AMPLITUDE)
                           } else {
                              VibrationEffect.createOneShot(CLICK_DURATION, VibrationEffect.DEFAULT_AMPLITUDE)
                           }
                        }
                     )
                  }
                  dragging = true
                  lastDragIndex = index

                  coroutineScope.launch {
                     handleLazyListScroll(
                        lazyListState = lazyListState,
                        dropIndex = index + 3,
                        density = density,
                     )
                  }
               }
            },
            onDrop = {
               vibrator?.vibrate(
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                     VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                  } else {
                     VibrationEffect.createOneShot(CLICK_DURATION, VibrationEffect.DEFAULT_AMPLITUDE)
                  }
               )
               dragging = false
               lastDragIndex = -1
               setOrder(reorderingList.indexOf(it.data))
            },
            draggableContent = {
               content(Modifier)
            },
            modifier = modifier,
         ) {
            content(
               Modifier.graphicsLayer {
                  alpha = if (isDragging) 0f else 1f
               }
            )
         }
      }
   }

   ReorderContainer(
      reorderState,
      enabled = enabled,
      modifier = modifier,
   ) {
      scope.content(reorderingList)
   }
}

@Stable
interface ReorderableListScope<T> {
   @Composable
   fun ReorderableListItem(
      key: Any,
      data: T,
      setOrder: (toIndex: Int) -> Unit,
      modifier: Modifier = Modifier,
      content: @Composable ((Modifier) -> Unit),
   )
}
