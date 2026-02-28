plugins {
   pureKotlinModule
   alias(libs.plugins.serialization)
}

dependencies {
   api(libs.kotlin.coroutines)
   api(projects.appstore.api)
   api(libs.kotlinova.core)

   implementation(libs.kotlin.serialization.core)

   compileOnly(libs.androidx.compose.runtime.annotation)
}
