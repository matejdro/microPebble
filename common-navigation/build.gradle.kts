plugins {
   kmpLibraryModule
   id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
   androidLibrary {
      namespace = "com.matejdro.micropebble.navigation"
   }

   sourceSets {
      androidMain.dependencies {
         api(libs.kotlinova.navigation)
         implementation(libs.androidx.activity.compose)
         implementation(libs.androidx.core)
         implementation(libs.kotlin.serialization.core)
         implementation(libs.compose.animation)
         implementation(libs.compose.ui)
      }
   }
}
