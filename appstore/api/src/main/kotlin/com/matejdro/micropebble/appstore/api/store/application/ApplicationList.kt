package com.matejdro.micropebble.appstore.api.store.application

import androidx.compose.runtime.Immutable
import com.matejdro.micropebble.appstore.api.PaginationLinks
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ApplicationList(
   val data: List<Application>,
   val limit: Int,
   val links: PaginationLinks,
   val offset: Int,
)
