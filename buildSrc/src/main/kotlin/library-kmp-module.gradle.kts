import dev.detekt.gradle.Detekt
import tasks.setupTooManyKotlinFilesTaskForCommon

plugins {
   id("org.jetbrains.kotlin.multiplatform")
   id("com.android.kotlin.multiplatform.library")
   id("org.jetbrains.kotlin.plugin.compose")
   id("org.jetbrains.compose")

   id("checks")
}

val isMac = System.getProperty("os.name").contains("mac", ignoreCase = true)

kotlin {
   jvmToolchain(21)

   androidLibrary {
      // Modules with resources must override this.
      namespace = "com.matejdro.micropebble.noresources"
      compileSdk = 36
      minSdk = 30
   }

   if (isMac) {
      iosX64()
      iosArm64()
      iosSimulatorArm64()
   }

   compilerOptions {
      optIn.add("kotlin.time.ExperimentalTime")
      optIn.add("kotlin.uuid.ExperimentalUuidApi")
      optIn.add("kotlin.ExperimentalUnsignedTypes")
      freeCompilerArgs.add("-Xannotation-default-target=param-property")
   }

   sourceSets {
      commonMain.dependencies {
         implementation(compose.runtime)
      }
   }
}

if (name.startsWith("common-")) {
   setupTooManyKotlinFilesTaskForCommon()
}

tasks.register("runDebugDetekt") {
   dependsOn(tasks.withType<Detekt>())
}
