plugins {
   androidLibraryModule
   compose
   parcelize
}

dependencies {
   api(libs.kotlinova.navigation)
   implementation(projects.appstore.api)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.compose.material3.sizeClasses)
   implementation(libs.kotlin.serialization.json)
   implementation(libs.kotlinova.compose)
}
