package com.matejdro.micropebble.reporting

import com.matejdro.micropebble.common.logging.ActionLogger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import logcat.logcat

@ContributesBinding(AppScope::class)
@Inject
class LogcatActionLogger : ActionLogger {
   override fun logAction(text: () -> String) {
      logcat(message = text, tag = "UserAction")
   }
}
