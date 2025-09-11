plugins {
   androidLibraryModule
   di
}

android {
   namespace = "com.matejdro.micropebble.bluetooth"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.bluetooth.api)
   api(projects.commonAndroid)
   implementation(libs.libpebble3)
   implementation(libs.kotlin.coroutines)
}
