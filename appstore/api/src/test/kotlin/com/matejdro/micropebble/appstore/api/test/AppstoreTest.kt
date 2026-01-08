package com.matejdro.micropebble.appstore.api.test

import com.matejdro.micropebble.appstore.api.store.home.AppstoreHomePage
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class AppstoreTest {
   val testStrings = listOf(
      javaClass.getResource("/test-appstore-response.json")!!.readText(),
      javaClass.getResource("/test-appstore-response-apps.json")!!.readText()
   )

   @Test
   fun testHomepageDeserialization() {
      val json = Json {
         isLenient = true // ignoreUnknownKeys = true
      }

      for (string in testStrings) {
         assertDoesNotThrow {
            json.decodeFromString<AppstoreHomePage>(string)
         }
      }
   }
}
