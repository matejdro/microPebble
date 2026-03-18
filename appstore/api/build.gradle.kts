plugins {
   multiplatformModule
   serialization
}

kotlin {
   sourceSets {
      commonMain.dependencies {
         api(projects.common)
         api(libs.kotlin.coroutines)
         api(libs.kotlinova.core)
         api(libs.kotlin.serialization.json)
         api(libs.ktor.io)
         implementation(libs.kotlinx.datetime)

         compileOnly(libs.androidx.compose.runtime.annotation)
      }
   }
}
