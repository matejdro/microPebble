package com.matejdro.micropebble.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import org.tinylog.Level
import org.tinylog.provider.LoggingProvider
import org.tinylog.provider.ProviderRegistry

class TinyLogKermitWriter(
   private val provider: LoggingProvider = ProviderRegistry.getLoggingProvider(),
) : LogWriter() {
   override fun log(
      severity: Severity,
      message: String,
      tag: String,
      throwable: Throwable?,
   ) {
      val tinylogLevel = when (severity) {
         Severity.Verbose -> Level.TRACE
         Severity.Debug -> Level.DEBUG
         Severity.Info -> Level.INFO
         Severity.Warn -> Level.WARN
         Severity.Error, Severity.Assert -> Level.ERROR
      }

      provider.log(1, "LibPebble-$tag", tinylogLevel, throwable, null, message)
   }
}
