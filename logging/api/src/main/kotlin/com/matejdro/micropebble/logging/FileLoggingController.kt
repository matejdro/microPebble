package com.matejdro.micropebble.logging

import java.io.File

interface FileLoggingController {
   fun flush()

   fun getLogFolder(): File

   fun getDeviceInfo(): String
}
