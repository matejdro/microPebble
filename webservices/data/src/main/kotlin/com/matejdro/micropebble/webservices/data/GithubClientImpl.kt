package com.matejdro.micropebble.webservices.data

import android.content.Context
import com.matejdro.micropebble.webservices.api.GithubAsset
import com.matejdro.micropebble.webservices.api.GithubRelease
import com.matejdro.micropebble.webservices.api.GithubSource
import dev.zacsweers.metro.Inject
import dispatch.core.withIO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import logcat.logcat
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.Outcome
import java.io.File
import java.io.FileOutputStream
import java.time.Instant

/**
 * Implementation of GitHub API client for fetching releases and downloading assets.
 */
@Inject
class GithubClientImpl(
    private val context: Context,
) {
    
    companion object {
        private const val GITHUB_API_BASE = "https://api.github.com"
        private const val USER_AGENT = "microPebble"
        
        private val json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
    
    private var client: HttpClient? = null
    
    private suspend fun getHttp(): HttpClient = client ?: withIO {
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }
    }.also { client = it }
    
    /**
     * Fetch all releases for a GitHub repository.
     * 
     * @param source The GitHub source (owner/repo)
     * @param token Optional GitHub personal access token for authenticated requests
     * @return Outcome with list of releases or error
     */
    suspend fun fetchReleases(
        source: GithubSource,
        token: String? = null
    ): Outcome<List<GithubRelease>> = withContext(Dispatchers.IO) {
        try {
            logcat { "GithubClientImpl.fetchReleases: Fetching from ${source.owner}/${source.repo}" }
            
            val response: HttpResponse = getHttp().get("$GITHUB_API_BASE/repos/${source.owner}/${source.repo}/releases") {
                headers {
                    append("User-Agent", USER_AGENT)
                    token?.let { append("Authorization", "Bearer $it") }
                    append("Accept", "application/vnd.github.v3+json")
                }
            }
            
            logcat { "GithubClientImpl.fetchReleases: Response status = ${response.status}" }
            
            if (response.status != HttpStatusCode.OK) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    ""
                }
                logcat { "GithubClientImpl.fetchReleases: ERROR - ${response.status}: $errorBody" }
                return@withContext Outcome.Error(UnknownCauseException(
                    cause = Exception("GitHub API returned ${response.status}: $errorBody")
                ))
            }
            
            // Parse the response - note that GitHub returns an array directly
            val releases: List<GithubReleaseDto> = response.body()
            logcat { "GithubClientImpl.fetchReleases: Parsed ${releases.size} releases" }
            
            // Filter out drafts and convert to our model
            val filteredReleases = releases
                .filter { !it.draft }
                .sortedByDescending { it.published_at }
                .map { releaseDto ->
                    // Filter out assets without download_url and map to domain
                    val assets = releaseDto.assets
                        .mapNotNull { assetDto ->
                            logcat { "  Asset: ${assetDto.name} (download_url: ${assetDto.download_url != null})" }
                            assetDto.toDomain()
                        }
                    val pbzAssets = assets.filter { it.name.endsWith(".pbz", ignoreCase = true) }
                    if (pbzAssets.isNotEmpty()) {
                        logcat { "  Found ${pbzAssets.size} PBZ assets: ${pbzAssets.joinToString { it.name }}" }
                    }
                    releaseDto.toDomain(assets)
                }
            
            logcat { "GithubClientImpl.fetchReleases: Filtered to ${filteredReleases.size} non-draft releases" }
            
            Outcome.Success(filteredReleases)
        } catch (e: SerializationException) {
            logcat { "GithubClientImpl.fetchReleases: Serialization error - ${e.message}" }
            Outcome.Error(UnknownCauseException(cause = Exception("Failed to parse GitHub response: ${e.message}")))
        } catch (e: Exception) {
            logcat { "GithubClientImpl.fetchReleases: Exception - ${e.message}" }
            e.printStackTrace()
            Outcome.Error(UnknownCauseException(cause = Exception("Failed to fetch GitHub releases: ${e.message}")))
        }
    }
    
    /**
     * Download a GitHub asset file.
     * 
     * @param asset The asset to download
     * @param token Optional GitHub personal access token
     * @param outputDir Directory to save the file (defaults to cache dir)
     * @return Outcome with the downloaded File or error
     */
    suspend fun downloadAsset(
        asset: GithubAsset,
        token: String? = null,
        outputDir: File? = null
    ): Outcome<File> = withContext(Dispatchers.IO) {
        try {
            val actualOutputDir = outputDir ?: context.cacheDir
            val outputFile = File(actualOutputDir, asset.name)
            
            // Ensure parent directory exists
            outputFile.parentFile?.mkdirs()
            
            val client = getHttp()
            val response: HttpResponse = client.get(asset.download_url) {
                headers {
                    append("User-Agent", USER_AGENT)
                    token?.let { append("Authorization", "Bearer $it") }
                    append("Accept", "application/octet-stream")
                }
            }
            
            // Check status first
            if (response.status != HttpStatusCode.OK) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    ""
                }
                return@withContext Outcome.Error(UnknownCauseException(
                    cause = Exception("Failed to download asset: ${response.status} - $errorBody")
                ))
            }
            
            // Write the response body to file
            val bytes = response.body<ByteArray>()
            FileOutputStream(outputFile).use { output ->
                output.write(bytes)
            }
            
            Outcome.Success(outputFile)
        } catch (e: Exception) {
            Outcome.Error(UnknownCauseException(cause = Exception("Failed to download asset: ${e.message}")))
        }
    }
    
    // DTO for deserializing GitHub API responses
    // GitHub API uses snake_case for some fields, and Instant needs custom handling
    @Serializable
    private data class GithubReleaseDto(
        val id: Long,
        val tag_name: String,
        val name: String?,
        val body: String?,
        val published_at: String?, // ISO-8601 string
        val assets: List<GithubAssetDto>,
        val html_url: String,
        val prerelease: Boolean = false,
        val draft: Boolean = false,
    ) {
        fun toDomain(assets: List<GithubAsset>): GithubRelease = GithubRelease(
            id = id,
            tag_name = tag_name,
            name = name,
            body = body,
            published_at = published_at?.let { Instant.parse(it) },
            assets = assets,
            html_url = html_url,
            is_prerelease = prerelease,
            is_draft = draft,
        )
    }
    
    @Serializable
    private data class GithubAssetDto(
        val id: Long,
        val name: String,
        val size: Long? = null,
        @SerialName("browser_download_url")
        val download_url: String? = null,
        val content_type: String? = null,
    ) {
        fun toDomain(): GithubAsset? {
            // Only create domain object if download_url is present
            if (download_url == null) return null
            return GithubAsset(
                id = id,
                name = name,
                size = size ?: 0L,
                download_url = download_url,
                browser_download_url = download_url,
                content_type = content_type ?: "",
            )
        }
    }
}
