plugins {
   androidLibraryModule
   compose
   parcelize
   showkase
}

android {
   namespace = "com.matejdro.micropebble.ui"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.compose)
   implementation(libs.kotlin.coroutines)
   implementation(libs.coil)
   implementation(libs.coil.okhttp)
}
