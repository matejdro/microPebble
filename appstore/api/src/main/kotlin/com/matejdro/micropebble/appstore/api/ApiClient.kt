package com.matejdro.micropebble.appstore.api

import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.collection.AppstoreCollectionPage
import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

interface ApiClient {
   val json: Json
   val http: HttpClient

   suspend fun fetchAppListing(updateSource: AppstoreSource, installSource: AppInstallSource): Application?
   suspend fun fetchAppListing(updateSource: AppstoreSource, appstoreId: String): Application
   suspend fun fetchHomePage(source: AppstoreSource, type: ApplicationType, platformFilter: String?): AppstoreHomePage
   suspend fun fetchCollection(platformFilter: String?, endpoint: String, offset: Int, limit: Int): AppstoreCollectionPage
}
