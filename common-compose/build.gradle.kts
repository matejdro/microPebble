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
   implementation(projects.common)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.compose)
   implementation(libs.kotlin.coroutines)
   implementation(libs.androidx.core)
   implementation(libs.coil)
   implementation(libs.coil.okhttp)
   implementation(libs.composeDnd)
}
