package com.matejdro.micropebble.common.exceptions

import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.reporting.ErrorReporter

/**
 * Exception that will, when reported to the [ErrorReporter], crash the app if app is in debug mode, to alert the developer
 * that something terrible (but recoverable) has happened. Otherwise, exception is just reported, to not annoy user in production.
 */
class CrashOnDebugException(message: String? = null, cause: Throwable? = null) :
   CauseException(message, cause)
