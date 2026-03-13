plugins {
   multiplatformModule
}

kotlin {
   sourceSets {
      commonMain.dependencies {
         api(libs.okio)
      }
   }
}
