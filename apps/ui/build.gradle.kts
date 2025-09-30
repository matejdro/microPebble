plugins {
   androidLibraryModule
   compose
   di
   navigation
   showkase
}

android {
   namespace = "com.matejdro.micropebble.apps.ui"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.common)
   api(libs.kotlinova.navigation)
   api(libs.kotlin.coroutines)

   implementation(projects.commonCompose)
   implementation(projects.commonNavigation)
   implementation(libs.kotlinova.core)
   implementation(libs.libpebble3)
}
