plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.logging.api)
   api(projects.common)
   api(libs.androidx.core)
   api(libs.dispatch)
   api(libs.kermit)
   api(libs.tinylog.api)
   api(libs.tinylog.impl)
   implementation(libs.kotlin.coroutines)
   implementation(libs.logcat)
}
