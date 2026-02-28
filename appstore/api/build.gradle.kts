plugins {
   pureKotlinModule
   serialization
}

dependencies {
   api(projects.common)
   api(libs.kotlin.coroutines)
   api(libs.kotlinova.core)
   api(libs.kotlin.serialization.json)
   api(libs.ktor.io)

   compileOnly(libs.androidx.compose.runtime.annotation)
}
