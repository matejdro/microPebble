plugins {
   pureKotlinModule
   serialization version libs.versions.kotlin.serialization
}

dependencies {
   implementation(libs.kotlin.serialization.json)
   implementation(libs.kotlin.coroutines)
}