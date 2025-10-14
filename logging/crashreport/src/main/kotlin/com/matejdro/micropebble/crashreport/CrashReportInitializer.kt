package com.matejdro.micropebble.crashreport

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import androidx.core.content.getSystemService
import androidx.startup.Initializer

/**
 * CrashReportInitializer is an [androidx.startup.Initializer] for CrashReport
 *
 * Based on the https://github.com/fvito/WhatTheStack/blob/9986dcc0abd5f90b03c2bcfa53120dad40617812/what-the-stack/src/main/java/com/haroldadmin/whatthestack/WhatTheStackInitializer.kt
 */
internal class CrashReportInitializer : Initializer<CrashReportInitializer.InitializedToken> {

   /**
    * Runs in the host app's process to:
    *
    * 1. Start [CrashReportService] as a bound service to allow communication between the
    * app's process and the service's process
    * 2. Replace the app's default [Thread.UncaughtExceptionHandler] with [CrashReportExceptionHandler]
    * when the service is connected.
    *
    * This method does not need to return anything, but it is required to return
    * a sensible value here so we return a dummy object [InitializedToken] instead.
    */
   override fun create(context: Context): InitializedToken {
      val messengerHolder = MessengerHolder(null)

      val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
      val customExceptionHandler = CrashReportExceptionHandler(
         context,
         messengerHolder,
         defaultExceptionHandler
      )
      Thread.setDefaultUncaughtExceptionHandler(customExceptionHandler)

      val connection = object : ServiceConnection {
         override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            messengerHolder.serviceMessenger = Messenger(service)
         }

         override fun onServiceDisconnected(name: ComponentName?) = Unit
      }

      val intent = Intent(context, CrashReportService::class.java)
      val importance = context.getSystemService<ActivityManager>()
         ?.runningAppProcesses
         ?.firstOrNull { it.pkgList.contains(context.packageName) }
         ?.importance

      if (importance != null &&
         importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
      ) {
         // Start service so that it outlives our app in case of a startup crash
         // Only start if our app is considered to be in the foreground,
         // otherwise Android will crash us
         context.startService(intent)
      }
      context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

      return InitializedToken
   }

   override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

   /**
    * A dummy object that does nothing but represent a type that can be returned by
    * [CrashReportInitializer]
    */
   internal object InitializedToken
}

internal class MessengerHolder(var serviceMessenger: Messenger?)
