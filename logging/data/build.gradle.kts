plugins {
   pureKotlinModule
   di
}

dependencies {
   api(projects.common)
   api(libs.kermit)
   api(libs.tinylog.api)
   api(libs.tinylog.impl)
   api(libs.kotlinova.core)
}
