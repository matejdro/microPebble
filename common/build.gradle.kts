plugins {
   multiplatformModule
}

kotlin {
   sourceSets {
      commonMain.dependencies {
         implementation(libs.kotlinova.core)
      }
   }
}
