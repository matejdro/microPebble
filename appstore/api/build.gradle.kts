plugins {
   pureKotlinModule
   serialization version libs.versions.kotlin.serialization
}

dependencies {
   api(projects.common)
   api(libs.kotlin.coroutines)
   api(libs.kotlinova.core)
   api(libs.kotlin.serialization.core)
   api(libs.kotlin.serialization.json)
   api(libs.ktor.io)
}
