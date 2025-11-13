package com.matejdro.micropebble.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.juul.kable.NotConnectedException
import si.inova.kotlinova.core.reporting.ErrorReporter

class ErrorReportingKermitWriter(
   private val errorReporter: ErrorReporter,
) : LogWriter() {
   override fun log(
      severity: Severity,
      message: String,
      tag: String,
      throwable: Throwable?,
   ) {
      if (throwable != null &&
         !EXCLUDED_TYPES.contains(throwable.javaClass) &&
         EXCLUDED_MESSAGES.none { throwable.message?.contains(it) == true }
      ) {
         errorReporter.report(throwable)
      }
   }
}

private val EXCLUDED_MESSAGES = listOf(
   "We don't handle resetting PPoG - disconnect and reconnect"
)

private val EXCLUDED_TYPES: List<Class<out Throwable>> = listOf(
   NotConnectedException::class.java
)
