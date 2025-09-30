package com.matejdro.micropebble.apps.ui.errors

import si.inova.kotlinova.core.outcome.CauseException

class InvalidPbwFileException : CauseException(isProgrammersFault = false)
