plugins {
   androidLibraryModule
   di
   alias(libs.plugins.serialization)
}

android {

   namespace = "com.matejdro.micropebble.appstore.data"
   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.webservices.api)
   api(libs.kotlin.coroutines)
   api(libs.kotlin.serialization.json)
   api(libs.dispatch)
   api(projects.appstore.api)

   implementation(libs.ktor.http)
   implementation(libs.ktor.serialization)
   implementation(libs.ktor.utils)
   implementation(libs.ktor.client.core)
   implementation(libs.kotlin.serialization.core)
   implementation(libs.kotlinova.core)
   implementation(libs.androidx.datastore.preferences)
   implementation(libs.androidx.work.runtime)
   implementation(libs.ktor.contentNegotiation)
   implementation(libs.ktor.serialization.kotlinx.json)
   implementation(libs.ktor.okhttp)

   compileOnly(libs.jspecify)
}
