package com.matejdro.micropebble.logging

import org.tinylog.Level
import org.tinylog.provider.LoggingProvider
import org.tinylog.provider.ProviderRegistry
import si.inova.kotlinova.core.logging.LogPriority
import si.inova.kotlinova.core.logging.LogcatLogger

class TinyLogLogcatLogger(
   private val provider: LoggingProvider = ProviderRegistry.getLoggingProvider(),
) : LogcatLogger {
   override fun log(priority: LogPriority, tag: String, message: String) {
      val tinylogLevel = when (priority) {
         LogPriority.VERBOSE -> Level.TRACE
         LogPriority.DEBUG -> Level.DEBUG
         LogPriority.INFO -> Level.INFO
         LogPriority.WARN -> Level.WARN
         LogPriority.ERROR, LogPriority.ASSERT -> Level.ERROR
      }

      provider.log(1, tag, tinylogLevel, null, null, message)
   }
}
