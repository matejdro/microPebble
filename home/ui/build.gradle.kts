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
   api(projects.notification.api)
   api(projects.common)
   api(libs.kotlinova.core)
   api(libs.kotlinova.navigation)
   implementation(projects.commonCompose)
   implementation(projects.sharedResources)
   implementation(libs.accompanist.permissions)
   implementation(libs.kotlin.coroutines)
   implementation(libs.libpebble3)
}
