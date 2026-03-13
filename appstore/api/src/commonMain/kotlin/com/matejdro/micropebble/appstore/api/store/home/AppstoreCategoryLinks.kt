package com.matejdro.micropebble.appstore.api.store.home

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AppstoreCategoryLinks(
   val apps: String,
)
