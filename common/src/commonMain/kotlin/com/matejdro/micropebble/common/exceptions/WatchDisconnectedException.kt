package com.matejdro.micropebble.common.exceptions

import si.inova.kotlinova.core.outcome.CauseException

class WatchDisconnectedException(
   message: String? = null,
   cause: Throwable? = null,
) : CauseException(message, cause, isProgrammersFault = false)
