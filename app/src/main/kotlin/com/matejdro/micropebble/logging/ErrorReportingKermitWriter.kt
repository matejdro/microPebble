package com.matejdro.micropebble.logging

import android.os.OperationCanceledException
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.juul.kable.GattStatusException
import com.juul.kable.GattWriteException
import com.juul.kable.NotConnectedException
import io.rebble.libpebblecommon.connection.ConnectionException
import io.rebble.libpebblecommon.services.PutBytesService
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
   "We don't handle resetting PPoG - disconnect and reconnect",
   "Can't reduce MTU",
   "expected ResetComplete",
   "Couldn't send packet",
   "Cannot connect peripheral that has been cancelled",
)

// We don't want to report generic BLE disconnections as they are often non-actionable
// (for example, when watch goes out of range)
private val EXCLUDED_TYPES: List<Class<out Throwable>> = listOf(
   NotConnectedException::class.java,
   GattWriteException::class.java,
   GattStatusException::class.java,
   PutBytesService.PutBytesException::class.java,
   ConnectionException::class.java,
   OperationCanceledException::class.java,
)
