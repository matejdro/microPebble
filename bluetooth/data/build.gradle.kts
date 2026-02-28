plugins {
   androidLibraryModule
   di
}

android {
   namespace = "com.matejdro.micropebble.bluetooth"

   androidResources.enable = true
}

dependencies {
   api(projects.bluetooth.api)
   api(projects.commonAndroid)
   api(projects.voice.api)
   api(libs.dispatch)
   implementation(projects.sharedResources)
   implementation(libs.libpebble3)
   implementation(libs.kotlin.coroutines)
}
