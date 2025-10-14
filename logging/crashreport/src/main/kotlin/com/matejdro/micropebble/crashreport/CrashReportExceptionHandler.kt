package com.matejdro.micropebble.crashreport

import android.content.Context
import android.os.Message
import androidx.core.os.bundleOf

/**
 * A [Thread.UncaughtExceptionHandler] which is meant to be used as a default exception handler on
 * the application.
 *
 * It runs in the host app's process to:
 * 1. Process any exception it catches and forward the result in a [Message] to [CrashReportService]
 * 2. Call the default exception handler it replaced, if any
 * 3. Kill the app process if there was no previous default exception handler
 *
 * Based on the https://github.com/fvito/WhatTheStack/blob/9986dcc0abd5f90b03c2bcfa53120dad40617812/what-the-stack/src/main/java/com/haroldadmin/whatthestack/WhatTheStackExceptionHandler.kt
 */
internal class CrashReportExceptionHandler(
   private val context: Context,
   private val messengerHolder: MessengerHolder,
   private val defaultHandler: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {
   override fun uncaughtException(t: Thread, e: Throwable) {
      e.printStackTrace()
      val serviceMessenger = messengerHolder.serviceMessenger

      val exceptionText = e.stackTraceToString().take(CrashReportService.CRASH_TEXT_LIMIT)

      if (serviceMessenger != null) {
         serviceMessenger.send(
            Message().apply {
               data = bundleOf(CrashReportActivity.EXTRA_TEXT to exceptionText)
            }
         )
      } else {
         // Service has not started yet. Wait a second
         Thread.sleep(SERVICE_START_WAIT_MS)

         CrashReportService.getStartupCrashFile(context).writeText(exceptionText)
      }

      defaultHandler?.uncaughtException(t, e)
   }
}

private const val SERVICE_START_WAIT_MS = 1_000L
