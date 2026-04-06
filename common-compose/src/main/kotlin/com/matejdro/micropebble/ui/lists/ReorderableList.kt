package com.matejdro.micropebble.ui.lists

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.mohamedrejeb.compose.dnd.reorder.ReorderState
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import kotlinx.coroutines.launch

@Composable
fun <T> ReorderableListContainer(
   data: List<T>,
   lazyListState: LazyListState,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   content: @Composable ReorderableListScope<T>.(List<T>) -> Unit,
) {
   val reorderState = remember<ReorderState<T>>(data) {
      ReorderState(
         dragAfterLongPress = true,
      )
   }

   var reorderingList by remember(data) { mutableStateOf(data) }
   var dragging by remember(reorderState) { mutableStateOf(false) }
   val density = LocalDensity.current
   val vibrator = LocalContext.current.getSystemService<Vibrator>()
   val coroutineScope = rememberCoroutineScope()

   var lastDragIndex by remember(reorderState) { mutableIntStateOf(-1) }

   val scope = object : ReorderableListScope<T> {
      @Composable
      override fun ReorderableListItem(
         key: String,
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
                        VibrationEffect.createPredefined(
                           if (dragging) {
                              VibrationEffect.EFFECT_TICK
                           } else {
                              VibrationEffect.EFFECT_CLICK
                           }
                        )
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
            onDrop = { dropState ->
               vibrator?.vibrate(
                  VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
               )
               dragging = false
               lastDragIndex = -1
               setOrder(reorderingList.indexOf(dropState.data))
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

   key(reorderState) {
      ReorderContainer(
         reorderState,
         enabled = enabled,
         modifier = modifier,
      ) {
         scope.content(reorderingList)
      }
   }
}

@Stable
interface ReorderableListScope<T> {
   @Composable
   fun ReorderableListItem(
      key: String,
      data: T,
      setOrder: (toIndex: Int) -> Unit,
      modifier: Modifier = Modifier,
      content: @Composable ((Modifier) -> Unit),
   )
}
