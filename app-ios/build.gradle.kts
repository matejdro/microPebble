plugins {
   id("org.jetbrains.kotlin.multiplatform")
   id("org.jetbrains.kotlin.plugin.compose")
   id("org.jetbrains.compose")
}

kotlin {
   listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
      target.binaries.framework {
         baseName = "Shared"
      }
   }

   sourceSets {
      iosMain.dependencies {
         implementation(projects.home.ui)
         implementation(libs.compose.runtime)
         implementation(libs.compose.foundation)
         implementation(libs.compose.material3)
         implementation(libs.compose.ui)
      }
   }
}
