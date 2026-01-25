plugins {
   androidLibraryModule
   compose
   di
   navigation
   showkase
}

android {
   namespace = "com.matejdro.micropebble.bluetooth.ui"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.bluetooth.api)
   api(projects.commonNavigation)
   api(projects.common)
   api(projects.notification.api)
   api(libs.kotlinova.navigation)
   api(libs.accompanist.permissions)

   implementation(projects.commonCompose)
   implementation(projects.sharedResources)
   implementation(libs.androidx.core)
   implementation(libs.androidx.activity.compose)
   implementation(libs.dispatch)
   implementation(libs.libpebble3)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlin.io)
   implementation(libs.kotlinova.core)
   implementation(libs.okio)
}
