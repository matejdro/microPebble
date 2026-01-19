package com.matejdro.micropebble.appstore.data

import com.matejdro.micropebble.appstore.api.ApiClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

@Inject
@ContributesBinding(AppScope::class)
class ApiClientImpl : ApiClient {
   override val json: Json = Json {
      isLenient = true
      ignoreUnknownKeys = true
   }

   override val http by lazy {
      runBlocking(Dispatchers.IO) {
         HttpClient {
            install(ContentNegotiation) {
               json(json)
            }
         }
      }
   }
}
