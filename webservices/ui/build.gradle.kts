plugins {
   androidLibraryModule
   compose
   di
   navigation
   parcelize
   serialization
   showkase
}

android {
   namespace = "com.matejdro.micropebble.webservices.ui"

   androidResources.enable = true
}

dependencies {
   api(projects.webservices.api)
   api(projects.common)
   api(libs.kotlinova.navigation)
   api(libs.kotlin.coroutines)
   api(projects.appstore.api)
   api(libs.kotlinova.core)

   implementation(projects.commonCompose)
   implementation(projects.commonNavigation)
   implementation(projects.sharedResources)
   implementation(libs.kotlin.coroutines)
}
