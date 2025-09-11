package com.matejdro.micropebble.reporting

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import com.matejdro.micropebble.common.logging.ActionLogger
import si.inova.kotlinova.core.logging.logcat

@ContributesBinding(AppScope::class)
class LogcatActionLogger @Inject constructor() : ActionLogger {
   override fun logAction(text: () -> String) {
      logcat(message = text)
   }
}
