package com.matejdro.micropebble.di

import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import com.matejdro.micropebble.MainViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dispatch.core.DefaultCoroutineScope
import io.rebble.libpebblecommon.connection.LibPebble
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.navigation.conditions.ConditionalNavigationHandler
import si.inova.kotlinova.navigation.deeplink.MainDeepLinkHandler
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import kotlin.reflect.KClass

@DependencyGraph(AppScope::class, isExtendable = true, additionalScopes = [OuterNavigationScope::class])
interface MainApplicationGraph : ApplicationGraph {
   @DependencyGraph.Factory
   interface Factory {
      fun create(
         @Provides
         application: Application,
      ): MainApplicationGraph
   }
}

@Suppress("ComplexInterface") // DI
interface ApplicationGraph {
   fun getErrorReporter(): ErrorReporter
   fun getDefaultCoroutineScope(): DefaultCoroutineScope
   fun getNavigationInjectionFactory(): NavigationInjection.Factory
   fun getMainDeepLinkHandler(): MainDeepLinkHandler
   fun getNavigationContext(): NavigationContext
   fun getDateFormatter(): AndroidDateTimeFormatter
   fun getMainViewModelFactory(): MainViewModel.Factory
   fun initLibPebble(): LibPebble

   @Multibinds(allowEmpty = true)
   fun provideEmptyConditionalMultibinds(): Map<KClass<*>, ConditionalNavigationHandler>

   @Multibinds
   fun provideServiceFactories(): Map<KClass<out Service>, Provider<Service>>

   @Multibinds
   fun provideReceiverFactories(): Map<KClass<out BroadcastReceiver>, Provider<BroadcastReceiver>>
}
