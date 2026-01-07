package com.matejdro.micropebble.appstore.api.test

import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class AppstoreTest {
   val testString = javaClass.getResource("/test-appstore-response.json")!!.readText()

   @Test
   fun testHomepageDeserialization() {
      val json = Json {
         isLenient = true // ignoreUnknownKeys = true
      }

      json.decodeFromString<AppstoreHomePage>(testString)
   }
}
