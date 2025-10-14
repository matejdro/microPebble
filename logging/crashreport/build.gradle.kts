plugins {
   androidLibraryModule
   compose
}

android {

   namespace = "com.matejdro.micropebble.crashreport"
   buildFeatures {
      androidResources = true
   }
}

dependencies {
   implementation(projects.commonCompose)
   // This module can exceptionally depend directly on the data as we want to keep this module independent from our
   // DI system, so crash reporting still works even if DI somehow fails
   implementation(projects.logging.data)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.androidx.startup)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
}
