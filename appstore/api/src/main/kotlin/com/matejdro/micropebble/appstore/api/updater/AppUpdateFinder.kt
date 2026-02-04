package com.matejdro.micropebble.appstore.api.updater

interface AppUpdateFinder {
   suspend fun findAndNotifyUpdates(): Boolean
}
