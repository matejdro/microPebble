plugins {
   androidLibraryModule
   compose
   di
   navigation
   showkase
}

android {
   namespace = "com.matejdro.micropebble.ui"
   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.bluetooth.api)
   api(projects.common)
   api(libs.kotlinova.core)
   api(libs.kotlinova.navigation)
   implementation(projects.commonCompose)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlin.datetime)
   implementation(libs.libpebble3)
}
