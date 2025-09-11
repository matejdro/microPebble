plugins {
   androidLibraryModule
   compose
   di
   navigation
}

android {
   namespace = "com.matejdro.micropebble.ui"
   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(libs.kotlinova.navigation)
}
