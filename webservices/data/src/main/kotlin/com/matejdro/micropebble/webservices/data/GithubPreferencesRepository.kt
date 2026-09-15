package com.matejdro.micropebble.webservices.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.matejdro.micropebble.webservices.api.GithubSource
import com.matejdro.micropebble.webservices.api.GithubToken
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dispatch.core.IOCoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

/**
 * Repository for storing GitHub-related preferences.
 * This includes:
 * - GitHub tokens (for private repo access)
 * - Custom GitHub sources (repos to check for firmware)
 * - Last checked timestamp
 */
@Serializable
data class GithubPreferences(
    val tokens: Map<Uuid, GithubToken> = emptyMap(),
    val customSources: List<GithubSource> = emptyList(),
    val lastCheckedSource: Uuid? = null,
) {
    fun getEnabledSources(): List<GithubSource> {
        val defaultSources = GithubSource.defaultSources
        val enabledDefaults = defaultSources.filter { it.enabled }
        return enabledDefaults + customSources.filter { it.enabled }
    }
}

@ContributesTo(AppScope::class)
interface GithubPreferencesProviders {
    @Provides
    @SingleIn(AppScope::class)
    fun provideGithubPreferencesStore(
        context: Context,
        ioCoroutineScope: IOCoroutineScope
    ): DataStore<GithubPreferences> {
        return DataStoreFactory.create(
            scope = ioCoroutineScope,
            serializer = GithubPreferencesSerializer
        ) {
            context.dataStoreFile("githubPreferences.json")
        }
    }
}

object GithubPreferencesSerializer : androidx.datastore.core.Serializer<GithubPreferences> {
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    override val defaultValue: GithubPreferences = GithubPreferences()

    override suspend fun readFrom(input: java.io.InputStream): GithubPreferences {
        return json.decodeFromString(GithubPreferences.serializer(), input.readBytes().decodeToString())
    }

    override suspend fun writeTo(t: GithubPreferences, output: java.io.OutputStream) {
        output.write(json.encodeToString(GithubPreferences.serializer(), t).toByteArray())
    }
}

/**
 * Repository class for accessing GitHub preferences.
 */
@Inject
class GithubPreferencesRepository(
    private val preferencesStore: DataStore<GithubPreferences>,
) {
    
    val preferences = preferencesStore.data
    
    suspend fun getTokenForSource(sourceId: Uuid): String? {
        return preferencesStore.data.first().tokens[sourceId]?.token
    }
    
    suspend fun saveToken(token: GithubToken) {
        preferencesStore.updateData { prefs ->
            val newTokens = prefs.tokens.toMutableMap().apply {
                put(token.sourceId, token)
            }
            prefs.copy(tokens = newTokens)
        }
    }
    
    suspend fun removeToken(sourceId: Uuid) {
        preferencesStore.updateData { prefs ->
            val newTokens = prefs.tokens.toMutableMap().apply {
                remove(sourceId)
            }
            prefs.copy(tokens = newTokens)
        }
    }
    
    suspend fun addCustomSource(source: GithubSource) {
        preferencesStore.updateData { prefs ->
            val existingSources = prefs.customSources.toMutableList()
            // Remove any existing source with the same ID
            existingSources.removeAll { it.id == source.id }
            existingSources.add(source)
            prefs.copy(customSources = existingSources)
        }
    }
    
    suspend fun removeCustomSource(sourceId: Uuid) {
        preferencesStore.updateData { prefs ->
            val newSources = prefs.customSources.filter { it.id != sourceId }
            prefs.copy(customSources = newSources)
        }
    }
    
    suspend fun setLastCheckedSource(sourceId: Uuid) {
        preferencesStore.updateData { prefs ->
            prefs.copy(lastCheckedSource = sourceId)
        }
    }
}
