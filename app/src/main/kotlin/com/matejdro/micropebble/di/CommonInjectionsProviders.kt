package com.matejdro.micropebble.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dispatch.core.IOCoroutineScope
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatterImpl
import si.inova.kotlinova.core.time.AndroidTimeProvider
import si.inova.kotlinova.core.time.DefaultAndroidTimeProvider
import si.inova.kotlinova.core.time.TimeProvider

@ContributesTo(AppScope::class)
interface CommonInjectionsProviders {
   @Provides
   fun bindToContext(application: Application): Context = application

   @Provides
   fun bindToTimeProvider(androidTimeProvider: AndroidTimeProvider): TimeProvider = androidTimeProvider

   @Provides
   fun provideAndroidDateTimeFormatter(
      context: Context,
      errorReporter: ErrorReporter,
   ): AndroidDateTimeFormatter = AndroidDateTimeFormatterImpl(context, errorReporter)

   @Provides
   fun provideAndroidTimeProvider(): AndroidTimeProvider {
      return DefaultAndroidTimeProvider
   }

   @Provides
   @SingleIn(AppScope::class)
   fun provideDefaultPreferences(context: Context, ioCoroutineScope: IOCoroutineScope): DataStore<Preferences> {
      return PreferenceDataStoreFactory.create(scope = ioCoroutineScope) {
         context.preferencesDataStoreFile("preferences")
      }
   }
}
