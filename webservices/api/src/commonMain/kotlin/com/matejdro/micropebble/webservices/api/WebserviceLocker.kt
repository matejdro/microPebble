package com.matejdro.micropebble.webservices.api

import com.matejdro.micropebble.appstore.api.store.application.Application
import kotlinx.serialization.Serializable

@Serializable
data class WebserviceLocker(
   val applications: List<Application>,
)
