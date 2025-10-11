package com.matejdro.micropebble.logging

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dispatch.core.IOCoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.tinylog.Level
import org.tinylog.provider.LoggingProvider
import org.tinylog.provider.ProviderRegistry

/**
 * Alternative implementation of the logging thread for the TinyLog.
 *
 * For the log sending, we need an ability to flush the logs before sending. However, flush command is not synchronized with
 * the TinyLog's native logging thread enabled, which means that we could cause a race condition by doing this. This is solved by
 * disabling the logging thread, but then logs are not being written on the background thread anymore. This class fixes this by
 * passing all logs to the background thread before writing.
 *
 * See https://github.com/tinylog-org/tinylog/issues/749
 */
@Inject
@SingleIn(AppScope::class)
class TinyLogLoggingThread(
   private val ioCoroutineScope: IOCoroutineScope,
   private val provider: LoggingProvider = ProviderRegistry.getLoggingProvider(),
) : Thread() {
   private val channel = Channel<LogEntry>(LOG_BUFFER_CAPACITY)

   fun log(
      depth: Int,
      tag: String,
      level: Level,
      message: String?,
      exception: Throwable? = null,
   ) {
      channel.trySend(
         LogEntry(
            depth,
            tag,
            level,
            message,
            exception
         )
      )
   }

   init {
      ioCoroutineScope.launch {
         channel.consumeEach { entry ->
            provider.log(entry.depth, entry.tag, entry.level, entry.exception, null, entry.message)
         }
      }
   }
}

private data class LogEntry(
   val depth: Int,
   val tag: String,
   val level: Level,
   val message: String?,
   val exception: Throwable? = null,
)

private const val LOG_BUFFER_CAPACITY = 128
