package com.matejdro.micropebble.appstore.api

import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome
import java.io.IOException
import java.net.URL
import kotlin.uuid.Uuid

enum class AppInstallState {
   CAN_INSTALL,
   INSTALLED,
}

class AppSideloadFailed : CauseException(message = "App failed to sideload", isProgrammersFault = false)
class AppDownloadFailed(cause: IOException) : CauseException(message = "App download failed", cause)

interface AppInstallationClient {
   suspend fun install(url: URL, tmpFileName: String = Uuid.random().toString()): Outcome<AppInstallState>
}
