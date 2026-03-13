package com.matejdro.micropebble.logging

import okio.Path

interface FileLoggingController {
   fun flush()

   fun getLogFolder(): Path

   fun getDeviceInfo(): String
}
