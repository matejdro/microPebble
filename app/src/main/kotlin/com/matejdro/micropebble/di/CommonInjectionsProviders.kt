package com.matejdro.micropebble.di

import android.app.Application
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
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
}
