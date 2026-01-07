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
   implementation(libs.ktor.okhttp)
   implementation(libs.ktor.contentNegotiation)
   implementation(libs.ktor.serialization.kotlinx.json)
}
