package com.matejdro.micropebble.apps.ui.util

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Adapted from https://github.com/MohamedRejeb/compose-dnd/blob/65d48ed0f0bd83a0b01263b7e046864bdd4a9048/sample/common/src/commonMain/kotlin/utils/ScrollUtils.kt
 * by MohamedRejeb
 */
suspend fun handleLazyListScroll(
   lazyListState: LazyListState,
   density: Density,
   dropIndex: Int,
): Unit = coroutineScope {
   val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
   val firstVisibleItemScrollOffset = lazyListState.firstVisibleItemScrollOffset

   val scrollPadding = with(density) { 32.dp.roundToPx() }

   // Workaround to fix scroll issue when dragging the first item
   if (dropIndex == 0 || dropIndex == 1) {
      launch {
         lazyListState.scrollToItem(firstVisibleItemIndex, firstVisibleItemScrollOffset)
      }
   }

   // Animate scroll when entering the first or last item
   val layoutInfo = lazyListState.layoutInfo

   val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull() ?: return@coroutineScope
   val scrollAmount = firstVisibleItem.size * 2f

   val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == dropIndex } ?: return@coroutineScope
   val endPosition = targetItem.offset + targetItem.size

   if (targetItem.offset - scrollPadding <= layoutInfo.viewportStartOffset) {
      launch {
         lazyListState.animateScrollBy(-scrollAmount)
      }
   } else if (endPosition + scrollPadding >= layoutInfo.viewportEndOffset) {
      launch {
         lazyListState.animateScrollBy(scrollAmount)
      }
   }
}
