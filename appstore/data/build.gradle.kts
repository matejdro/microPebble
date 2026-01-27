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
   api(projects.common)

   implementation(libs.ktor.serialization.kotlinx.json)
   implementation(libs.androidx.core)
   implementation(libs.androidx.datastore.preferences)
   implementation(libs.androidx.work.runtime)
   implementation(libs.ktor.contentNegotiation)
   implementation(libs.ktor.okhttp)
   implementation(libs.libpebble3)
   implementation(libs.dispatch)
}
