package com.matejdro.micropebble.appstore.ui.common

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

val json = Json {
   isLenient = true
   ignoreUnknownKeys = true
}

val httpClient by lazy {
   HttpClient {
      install(ContentNegotiation) {
         json(json)
      }
   }
}

suspend fun getHttpClient() = withContext(Dispatchers.IO) { httpClient }
