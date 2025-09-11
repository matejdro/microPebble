package com.matejdro.micropebble.di

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.core.app.AppComponentFactory
import dev.zacsweers.metro.Provider
import kotlin.reflect.KClass

class ComponentFactory : AppComponentFactory() {
   override fun instantiateServiceCompat(
      cl: ClassLoader,
      className: String,
      intent: Intent?,
   ): Service {
      return serviceFactories[Class.forName(className).kotlin]?.invoke()
         ?: createServiceWithEmptyConstructor(cl, className, intent)
   }

   override fun instantiateReceiverCompat(
      cl: ClassLoader,
      className: String,
      intent: Intent?,
   ): BroadcastReceiver {
      return receiverFactories[Class.forName(className).kotlin]?.invoke()
         ?: createReceiverWithEmptyConstructor(cl, className, intent)
   }

   private fun createReceiverWithEmptyConstructor(
      cl: ClassLoader,
      className: String,
      intent: Intent?,
   ): BroadcastReceiver {
      return try {
         super.instantiateReceiverCompat(cl, className, intent)
      } catch (e: Throwable) {
         throw IllegalStateException(
            "Receiver $className cannot be created. " +
               "Did you add @ContributesIntoMap and @ReceiverKey to it?",
            e
         )
      }
   }

   private fun createServiceWithEmptyConstructor(cl: ClassLoader, className: String, intent: Intent?): Service {
      return try {
         super.instantiateServiceCompat(cl, className, intent)
      } catch (e: Throwable) {
         throw IllegalStateException(
            "Service $className cannot be created. " +
               "Did you add @ContributesIntoMap and @ServiceKey to it?",
            e
         )
      }
   }

   companion object {
      lateinit var serviceFactories: Map<KClass<out Service>, Provider<Service>>
      lateinit var receiverFactories: Map<KClass<out BroadcastReceiver>, Provider<BroadcastReceiver>>
   }
}
