plugins {
   androidLibraryModule
   compose
   di
   navigation
   parcelize
   showkase
}

android {
   namespace = "com.matejdro.micropebble.home.ui"
   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.notification.api)
   api(projects.common)
   api(projects.logging.api)
   api(libs.androidx.core)
   api(libs.kotlinova.core)
   api(libs.kotlinova.navigation)

   implementation(projects.sharedResources)
   implementation(projects.commonCompose)
   implementation(libs.accompanist.permissions)
   implementation(libs.composePreference)
   implementation(libs.kotlin.coroutines)
   implementation(libs.dispatch)
   implementation(libs.androidx.compose.material3.sizeClasses)
   implementation(libs.libpebble3)
}
