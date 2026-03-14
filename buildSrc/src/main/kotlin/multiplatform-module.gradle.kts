import org.gradle.accessors.dm.LibrariesForLibs
import util.commonAndroid

val libs = the<LibrariesForLibs>()

plugins {
   id("org.jetbrains.kotlin.multiplatform")
   id("com.android.library")

   id("all-modules-commons")
}

kotlin {
   androidTarget()
   iosArm64()
   iosSimulatorArm64()
}

commonAndroid {
   namespace = "com.matejdro.micropebble.noresources"
   compileSdk = 36
   defaultConfig {
      minSdk = 30
   }
   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
   }
}

configure<com.android.build.gradle.LibraryExtension> {
   compileOptions.isCoreLibraryDesugaringEnabled = true
}

dependencies {
   add("coreLibraryDesugaring", libs.desugarJdkLibs)
   add("detektPlugins", project(":detekt"))
}
