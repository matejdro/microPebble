package com.matejdro.micropebble.appstore.api.store.collection

import androidx.compose.runtime.Immutable
import com.matejdro.micropebble.appstore.api.PaginationLinks
import com.matejdro.micropebble.appstore.api.store.application.Application
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AppstoreCollectionPage(
   @SerialName("data")
   val apps: List<Application>,
   val limit: Int,
   val offset: Int,
   val links: PaginationLinks,
)

fun AppstoreCollectionPage.filterApps(predicate: (app: Application) -> Boolean) = copy(
   apps = apps.filter(predicate)
)
