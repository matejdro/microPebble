package com.matejdro.micropebble.appstore.ui.common

import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.ui.unit.dp

internal const val BANNER_RATIO = 720f / 320f
internal val appGridCells: StaggeredGridCells = StaggeredGridCells.Adaptive(minSize = 150.dp)
