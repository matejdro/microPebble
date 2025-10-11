package com.matejdro.micropebble.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import org.tinylog.Level

class TinyLogKermitWriter(
   private val tinyLogLoggingThread: TinyLogLoggingThread,
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

      tinyLogLoggingThread.log(1, "LibPebble-$tag", tinylogLevel, message, throwable)
   }
}
