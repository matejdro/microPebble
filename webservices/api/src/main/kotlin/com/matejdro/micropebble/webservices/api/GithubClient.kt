package com.matejdro.micropebble.webservices.api

import si.inova.kotlinova.core.outcome.Outcome
import java.io.File

/**
 * Interface for fetching GitHub releases and downloading assets.
 */
interface GithubClient {
    suspend fun fetchReleases(
        source: GithubSource,
        token: String?
    ): Outcome<List<GithubRelease>>

    suspend fun downloadAsset(
        asset: GithubAsset,
        token: String?,
        outputDir: File?
    ): Outcome<File>
}
