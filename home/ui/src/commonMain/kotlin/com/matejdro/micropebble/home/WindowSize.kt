package com.matejdro.micropebble.home

import androidx.compose.runtime.Composable

/** True when the available width is the Material3 "Expanded" class (tablet-style two-pane layout). */
@Composable
expect fun isExpandedWidth(): Boolean
