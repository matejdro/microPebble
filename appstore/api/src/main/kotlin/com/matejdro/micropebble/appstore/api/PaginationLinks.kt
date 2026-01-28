package com.matejdro.micropebble.appstore.api

import kotlinx.serialization.Serializable

@Serializable
data class PaginationLinks(
   val nextPage: String?,
)
