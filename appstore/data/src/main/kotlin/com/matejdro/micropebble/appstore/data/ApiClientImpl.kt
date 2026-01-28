package com.matejdro.micropebble.appstore.data

import com.matejdro.micropebble.appstore.api.ApiClient
import com.matejdro.micropebble.appstore.api.AppInstallSource
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.store.application.ApplicationList
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.collection.AppstoreCollectionPage
import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import com.matejdro.micropebble.common.util.joinUrls
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Inject
@ContributesBinding(AppScope::class)
class ApiClientImpl : ApiClient {
   override val json: Json = Json {
      isLenient = true
      ignoreUnknownKeys = true
   }

   private var client: HttpClient? = null

   private suspend fun getHttp() = client ?: withContext(Dispatchers.IO) {
      HttpClient {
         install(ContentNegotiation) {
            json(json)
         }
      }
   }.also { client = it }

   override suspend fun fetchAppListing(updateSource: AppstoreSource, installSource: AppInstallSource) = runCatching {
      getHttp().get(updateSource.url.joinUrls("/v1/apps/id/${installSource.storeId}")).body<ApplicationList>().data.first()
   }.getOrNull()

   override suspend fun fetchAppListing(updateSource: AppstoreSource, appstoreId: String) =
      getHttp().get(updateSource.url.joinUrls("/v1/apps/id/$appstoreId")).body<AppstoreCollectionPage>().apps.first()

   override suspend fun fetchHomePage(
      source: AppstoreSource,
      type: ApplicationType,
      platformFilter: String?,
   ) = getHttp().get(source.url.joinUrls(type.apiEndpoint)) {
      url {
         platformFilter?.let { parameters["hardware"] = it }
      }
   }.body<AppstoreHomePage>()

   override suspend fun fetchCollection(platformFilter: String?, endpoint: String, offset: Int, limit: Int) =
      getHttp().get(endpoint) {
         url {
            platformFilter?.let { parameters["hardware"] = it }
            parameters["offset"] = offset.toString()
            parameters["limit"] = limit.toString()
         }
      }.body<AppstoreCollectionPage>()
}
