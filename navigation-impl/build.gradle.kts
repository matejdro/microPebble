plugins {
   androidLibraryModule
   compose
   di
   navigation
}

dependencies {
   api(libs.kotlinova.navigation)
   implementation(libs.androidx.compose.material3.sizeClasses)
   implementation(libs.androidx.navigation3)
   implementation(libs.kotlinova.compose)
   implementation(libs.kotlinova.navigation.navigation3)
}
