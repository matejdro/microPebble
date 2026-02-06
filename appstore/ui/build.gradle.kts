plugins {
   androidLibraryModule
   compose
   di
   navigation
   parcelize
   showkase
   alias(libs.plugins.serialization)
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
   api(libs.kotlinova.navigation)
   api(libs.kotlin.coroutines)

   implementation(projects.commonCompose)
   implementation(projects.commonNavigation)
   implementation(projects.sharedResources)
   implementation(libs.algolia)
   implementation(libs.coil)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlin.serialization.core)
   implementation(libs.kotlin.serialization.json)
   implementation(libs.androidx.foundation.layout)
   implementation(libs.androidx.paging.common)
   implementation(libs.androidx.paging.compose)
   implementation(libs.libpebble3)

   compileOnly(libs.koin.core)
}
