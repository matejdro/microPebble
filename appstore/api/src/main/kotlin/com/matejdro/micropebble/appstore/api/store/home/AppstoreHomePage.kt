package com.matejdro.micropebble.appstore.api.store.home

import com.matejdro.micropebble.appstore.api.store.application.Application
import kotlinx.serialization.Serializable

@Serializable
data class AppstoreHomePage(
   val applications: List<Application>,
   val banners: List<AppstoreBanner>,
   val categories: List<AppstoreCategory>,
   val collections: List<AppstoreCollection>,
) {
   val applicationsById by lazy { applications.associateBy { it.id } }
}
