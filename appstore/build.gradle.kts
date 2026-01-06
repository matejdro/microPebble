plugins {
   androidLibraryModule
   compose
   di
   navigation
   parcelize
   showkase
}

android {

   namespace = "com.matejdro.micropebble.appstore"
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
   implementation(projects.sharedResources)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.dispatch)
   implementation(libs.kotlin.io)
   implementation(libs.kotlinova.core)
   implementation(libs.okio)

   compileOnly(libs.koin.core)
}
