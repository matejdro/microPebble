plugins {
   androidLibraryModule
   di
}

android {

   namespace = "com.matejdro.micropebble.appstore.data"
   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.appstore.api)
   api(libs.kotlin.coroutines)
   api(libs.kotlin.serialization.json)
   api(libs.ktor.io)
   api(libs.dispatch)

   implementation(projects.common)
   implementation(libs.ktor.http)
   implementation(libs.ktor.serialization)
   implementation(libs.ktor.utils)
   implementation(libs.ktor.client.core)
   implementation(libs.androidx.core)
   implementation(libs.kotlin.io)
   implementation(libs.kotlin.serialization.core)
   implementation(libs.kotlinova.core)
   implementation(libs.jspecify)
   implementation(libs.androidx.datastore.preferences)
   implementation(libs.androidx.work.runtime)
   implementation(libs.ktor.contentNegotiation)
   implementation(libs.ktor.serialization.kotlinx.json)
   implementation(libs.ktor.okhttp)
   implementation(libs.libpebble3)
}
