package com.matejdro.micropebble.apps.ui.list

import androidx.compose.runtime.Stable

@Stable
data class WatchappListState(
   val watchfaces: List<WatchappListApp>,
   val watchapps: List<WatchappListApp>,
)

inline fun WatchappListState.map(block: (WatchappListApp) -> WatchappListApp) = WatchappListState(
   watchfaces = watchfaces.map(block),
   watchapps = watchapps.map(block),
)
