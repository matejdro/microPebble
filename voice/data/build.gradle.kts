plugins {
   androidLibraryModule
   di
}

android {
   namespace = "com.matejdro.micropebble.voice.data"
   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(libs.kotlinova.core)
   api(libs.libpebble3)
   api(libs.kotlin.coroutines)
   implementation(libs.speex)
   implementation(libs.androidx.annotation)
}
