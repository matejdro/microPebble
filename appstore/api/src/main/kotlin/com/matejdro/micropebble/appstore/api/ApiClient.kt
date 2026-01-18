package com.matejdro.micropebble.appstore.api

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

interface ApiClient {
   val json: Json
   val http: HttpClient
}
