package com.matejdro.micropebble.common.exceptions

import si.inova.kotlinova.core.outcome.CauseException

class LibPebbleError(

   message: String?,

   cause: Throwable? = null,
) :
   CauseException(message, cause, isProgrammersFault = message == null)
