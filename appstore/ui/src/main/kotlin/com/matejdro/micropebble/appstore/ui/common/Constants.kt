package com.matejdro.micropebble.appstore.ui.common

import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.ui.unit.dp

const val BANNER_RATIO = 720f / 320f
const val APP_IMAGE_ASPECT_RATIO = 6.0f / 7.0f
val appGridCells: StaggeredGridCells = StaggeredGridCells.Adaptive(minSize = 150.dp)
