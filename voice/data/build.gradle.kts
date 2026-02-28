plugins {
   androidLibraryModule
   di
}

android {
   namespace = "com.matejdro.micropebble.voice.data"
   androidResources.enable = true
}

dependencies {
   api(projects.commonAndroid)
   api(projects.voice.api)
   api(libs.androidx.core)
   api(libs.kotlinova.core)
   api(libs.libpebble3)
   api(libs.kotlin.coroutines)
   implementation(projects.sharedResources)
   implementation(libs.logcat)
   implementation(libs.speex)
   implementation(libs.androidx.annotation)
}
