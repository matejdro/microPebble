plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.logging.api)
   api(projects.common)
   api(libs.androidx.core)
   api(libs.kermit)
   api(libs.tinylog.api)
   api(libs.tinylog.impl)
   api(libs.kotlinova.core)
}
