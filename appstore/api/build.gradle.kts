plugins {
   pureKotlinModule
   serialization version libs.versions.kotlin.serialization
}

dependencies {
   api(libs.kotlin.coroutines)
   api(libs.kotlinova.core)
   api(libs.ktor.core)
   api(libs.kotlin.serialization.json)
}