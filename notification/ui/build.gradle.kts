plugins {
   androidLibraryModule
   compose
   di
}

android {
   namespace = "com.matejdro.micropebble.notification.ui"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.notification.api)

   testImplementation(projects.common.test)
}
