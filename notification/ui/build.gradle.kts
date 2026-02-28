plugins {
   androidLibraryModule
   compose
   di
   navigation
   serialization
   showkase
}

android {
   namespace = "com.matejdro.micropebble.notification.ui"

   androidResources.enable = true
}

dependencies {
   api(projects.common)
   implementation(projects.commonCompose)


   api(libs.kotlin.coroutines)
   api(libs.kotlinova.navigation)
   implementation(libs.accompanist.drawablepainter)
   implementation(libs.kotlinova.core)
   implementation(libs.libpebble3)
}
