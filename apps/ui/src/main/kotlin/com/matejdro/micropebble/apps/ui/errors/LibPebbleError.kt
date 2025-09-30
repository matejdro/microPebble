package com.matejdro.micropebble.apps.ui.errors

import si.inova.kotlinova.core.outcome.CauseException

class LibPebbleError(override val message: String) : CauseException(message, isProgrammersFault = false)
