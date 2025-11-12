package com.matejdro.micropebble.bluetooth.errors

import si.inova.kotlinova.core.outcome.CauseException

class InvalidPbzFileException : CauseException(isProgrammersFault = false)
