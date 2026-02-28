package com.matejdro.micropebble.logging

import logcat.LogPriority
import logcat.LogcatLogger

class MultiLogcatLogger(private val loggers: List<LogcatLogger>) : LogcatLogger {
   override fun log(priority: LogPriority, tag: String, message: String) {
      for (logger in loggers) {
         logger.log(priority, tag, message)
      }
   }
}
