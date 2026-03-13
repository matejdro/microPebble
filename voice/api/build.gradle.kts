plugins {
   multiplatformModule
}

kotlin {
   sourceSets {
      commonMain.dependencies {
         implementation(libs.kotlin.coroutines)
      }
   }
}
