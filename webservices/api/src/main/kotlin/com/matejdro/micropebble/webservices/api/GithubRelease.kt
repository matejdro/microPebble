package com.matejdro.micropebble.webservices.api

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class GithubRelease(
    val id: Long,
    val tag_name: String,
    val name: String?,
    val body: String?,
    @Contextual
    val published_at: Instant?,
    val assets: List<GithubAsset>,
    val html_url: String,
    val is_prerelease: Boolean = false,
    val is_draft: Boolean = false,
) {
    val pbzAssets: List<GithubAsset>
        get() = assets.filter { it.name.endsWith(".pbz", ignoreCase = true) }
}

@Serializable
data class GithubAsset(
    val id: Long,
    val name: String,
    val size: Long,
    val download_url: String,
    val browser_download_url: String,
    val content_type: String,
)
