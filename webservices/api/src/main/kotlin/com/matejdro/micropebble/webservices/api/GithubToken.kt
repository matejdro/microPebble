package com.matejdro.micropebble.webservices.api

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Token for GitHub API authentication.
 * This is separate from WebservicesToken which is for Rebble/Cobble services.
 */
@Stable
@Serializable
data class GithubToken(
    val sourceId: Uuid,
    val token: String,
) {
    companion object {
        const val DEFAULT_API_URL = "https://api.github.com/"
    }
}
