plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.notification.api)
   implementation(libs.androidx.core)
   implementation(libs.libpebble3)
}
