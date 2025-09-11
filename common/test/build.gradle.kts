plugins {
   pureKotlinModule
}

dependencies {
   implementation(libs.kotlin.coroutines.test)
   implementation(libs.kotlin.coroutines)
   implementation(libs.dispatch.test)
   implementation(libs.kotest.assertions)
   implementation(libs.turbine)
   implementation(libs.androidx.datastore.preferences.core)
}
