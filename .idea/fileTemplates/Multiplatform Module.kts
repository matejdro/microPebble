plugins {
   multiplatformModule
}

kotlin {
   sourceSets {
      commonMain.dependencies {
      }
      
      commonTest.dependencies {
         implementation(kotlin("test"))
      }
   }
}