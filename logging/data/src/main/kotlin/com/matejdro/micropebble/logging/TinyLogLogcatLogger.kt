package com.matejdro.micropebble.logging

import org.tinylog.Level
import si.inova.kotlinova.core.logging.LogPriority
import si.inova.kotlinova.core.logging.LogcatLogger

class TinyLogLogcatLogger(
   private val tinyLogLoggingThread: TinyLogLoggingThread,
) : LogcatLogger {
   override fun log(priority: LogPriority, tag: String, message: String) {
      val tinylogLevel = when (priority) {
         LogPriority.VERBOSE -> Level.TRACE
         LogPriority.DEBUG -> Level.DEBUG
         LogPriority.INFO -> Level.INFO
         LogPriority.WARN -> Level.WARN
         LogPriority.ERROR, LogPriority.ASSERT -> Level.ERROR
      }

      tinyLogLoggingThread.log(1, tag, tinylogLevel, message)
   }
}
