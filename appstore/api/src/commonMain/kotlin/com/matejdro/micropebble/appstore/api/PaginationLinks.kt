package com.matejdro.micropebble.appstore.api

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class PaginationLinks(
   val nextPage: String?,
)
