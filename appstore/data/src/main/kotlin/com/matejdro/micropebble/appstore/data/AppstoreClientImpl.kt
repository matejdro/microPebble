package com.matejdro.micropebble.appstore.data

import com.matejdro.micropebble.appstore.api.client.AppstoreClient
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Inject
@ContributesBinding(AppScope::class)
class AppstoreClientImpl : AppstoreClient {
   val client by lazy {
      HttpClient {
         install(ContentNegotiation) {
            json(
               Json {
                  isLenient = true
                  ignoreUnknownKeys = true
               }
            )
         }
      }
   }

   override suspend fun getHomePage(type: ApplicationType): AppstoreHomePage {
      return withContext(Dispatchers.IO) { client }.get("https://appstore-api.rebble.io/api/v1/home/faces?platform=all") {
         accept(ContentType.Application.Json)
      }.body()
   }
}
