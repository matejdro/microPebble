import com.autonomousapps.DependencyAnalysisSubExtension
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

configure<DependencyAnalysisSubExtension> {
   issues {
      onUsedTransitiveDependencies {
         // Transitive dependency detection is broken on the KMP projects
         // See https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/1345
         // It's not ideal, but it's best to at least use other aspects of the dependency analysis plugin
         excludeRegex(".*")
      }
   }
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
