plugins {
   androidLibraryModule
   compose
   di
   navigation
   parcelize
   showkase
}

android {

   namespace = "com.matejdro.micropebble.appstore.ui"
   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.appstore.api)
   api(projects.common)
   api(libs.kotlin.coroutines)

   implementation(projects.commonCompose)
   implementation(projects.commonNavigation)
   implementation(projects.sharedResources)
   implementation(libs.algolia)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.coil)
   implementation(libs.dispatch)
   implementation(libs.ktor.okhttp)
   implementation(libs.ktor.contentNegotiation)
   implementation(libs.ktor.serialization.kotlinx.json)
   implementation(libs.kotlin.io)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.navigation)
   implementation(libs.kotlin.serialization.json)
   implementation(libs.okio)
   implementation(libs.androidx.foundation.layout)
   implementation(libs.libpebble3)

   compileOnly(libs.koin.core)
}
