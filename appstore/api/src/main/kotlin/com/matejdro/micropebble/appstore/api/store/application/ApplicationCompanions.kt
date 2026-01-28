package com.matejdro.micropebble.appstore.api.store.application

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationCompanions(
   val android: ApplicationCompanion? = null,
   val ios: ApplicationCompanion? = null,
)
