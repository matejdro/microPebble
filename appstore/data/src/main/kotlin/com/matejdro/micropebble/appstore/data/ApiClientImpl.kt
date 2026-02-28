package com.matejdro.micropebble.appstore.data

import android.util.Log
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
import dispatch.core.withIO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.CancellationException
import kotlinx.serialization.json.Json

@Inject
@ContributesBinding(AppScope::class)
@Suppress("MissingUseCall") // getClient() is saved, we must not close it
class ApiClientImpl : ApiClient {
   override val json: Json = Json {
      isLenient = true
      ignoreUnknownKeys = true
   }

   private var client: HttpClient? = null

   @Suppress("MissingUseCall") // Client is saved for later, so we can't close it
   private suspend fun getHttp() = client ?: withIO {
      HttpClient {
         install(ContentNegotiation) {
            json(json)
         }
      }
   }.also { client = it }

   override suspend fun fetchAppListing(updateSource: AppstoreSource, installSource: AppInstallSource) = try {
      getHttp().get(updateSource.url.joinUrls("/v1/apps/id/${installSource.storeId}")).body<ApplicationList>().data.first()
   } catch (e: CancellationException) {
      throw e
   } catch (e: Exception) {
      Log.e("ApiClientImpl", "fetchAppListing failed", e)
      null
   }

   override suspend fun fetchAppListing(updateSource: AppstoreSource, appstoreId: String) =
      getHttp().get(updateSource.url.joinUrls("/v1/apps/id/$appstoreId")).body<AppstoreCollectionPage>().apps.first()

   override suspend fun fetchHomePage(
      source: AppstoreSource,
      type: ApplicationType,
      platformFilter: String?,
   ) = getHttp().get(source.url.joinUrls(type.apiEndpoint)) {
      url {
         platformFilter?.let { hardware -> parameters["hardware"] = hardware }
      }
   }.body<AppstoreHomePage>()

   override suspend fun fetchCollection(platformFilter: String?, endpoint: String, offset: Int, limit: Int) =
      getHttp().get(endpoint) {
         url {
            platformFilter?.let { hardware -> parameters["hardware"] = hardware }
            parameters["offset"] = offset.toString()
            parameters["limit"] = limit.toString()
         }
      }.body<AppstoreCollectionPage>()

   override suspend fun openInputStream(url: String): ByteReadChannel {
      return getHttp().get(url).bodyAsChannel()
   }
}
