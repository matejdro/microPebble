package com.matejdro.micropebble.webservices.data

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * DI providers for GitHub-related classes.
 */
@ContributesTo(AppScope::class)
interface GithubProviders {
    
    @Provides
    @SingleIn(AppScope::class)
    fun provideGithubClient(context: Context): GithubClientImpl = GithubClientImpl(context)
    
    @Provides
    @SingleIn(AppScope::class)
    fun provideGithubPreferencesRepository(
        githubPreferencesStore: androidx.datastore.core.DataStore<GithubPreferences>
    ): GithubPreferencesRepository = GithubPreferencesRepository(githubPreferencesStore)
}
