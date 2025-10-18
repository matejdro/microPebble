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
   implementation(projects.voice.speexCodec)
   implementation(libs.androidx.annotation)
}
