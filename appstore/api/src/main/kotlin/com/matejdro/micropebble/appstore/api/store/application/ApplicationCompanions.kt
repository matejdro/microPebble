package com.matejdro.micropebble.appstore.api.store.application

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ApplicationCompanions(
   val android: ApplicationCompanion? = null,
   val ios: ApplicationCompanion? = null,
)
