plugins {
   androidLibraryModule
   compose
   parcelize
   serialization
}

dependencies {
   api(libs.kotlinova.navigation)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.androidx.compose.material3.sizeClasses)
   implementation(libs.kotlinova.compose)
}
