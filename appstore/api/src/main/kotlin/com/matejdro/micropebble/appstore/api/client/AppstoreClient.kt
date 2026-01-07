package com.matejdro.micropebble.appstore.api.client

import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage

interface AppstoreClient {
   suspend fun getHomePage(type: ApplicationType): AppstoreHomePage
}
