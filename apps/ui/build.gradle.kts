plugins {
   androidLibraryModule
   compose
   di
   navigation
   parcelize
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
   api(projects.appstore.api)
   api(libs.kotlinova.navigation)
   api(libs.kotlin.coroutines)

   implementation(projects.commonCompose)
   implementation(projects.commonNavigation)
   implementation(projects.sharedResources)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.composeWebview)
   implementation(libs.dispatch)
   implementation(libs.kotlin.io)
   implementation(libs.kotlinova.core)
   implementation(libs.okio)
   implementation(libs.libpebble3)

   compileOnly(libs.koin.core)
}
