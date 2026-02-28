import com.android.build.api.dsl.LibraryBuildFeatures
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.build.gradle.tasks.asJavaVersion
import dev.detekt.gradle.extensions.DetektExtension
import jacoco.setupJacocoMergingAndroid
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import util.commonAndroid
import util.commonAndroidComponents

val libs = the<LibrariesForLibs>()

plugins {
   id("org.jetbrains.kotlin.android")

   id("all-modules-commons")
   id("org.gradle.android.cache-fix")
}

val customConfig = extensions.create<CustomBuildConfiguration>("custom")

commonAndroid {
   // Use default namespace for no resources, modules that use resources must override this
   namespace = "com.matejdro.micropebble.noresources"

   compileSdk = 36

   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17

      isCoreLibraryDesugaringEnabled = true
   }

   defaultConfig {
      minSdk = 30

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
   }

   testOptions {
      unitTests.all {
         it.useJUnitPlatform()

         // Better test output
         it.systemProperty("kotest.assertions.collection.print.size", "300")
         it.systemProperty("kotest.assertions.collection.enumerate.size", "300")
      }

      if (pluginManager.hasPlugin("com.android.library")) {
         targetSdk = 33
      }
   }

   packaging {
      resources {
         excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
   }

   buildFeatures {
      buildConfig = false
      resValues = false
      shaders = false

      if (this is LibraryBuildFeatures) {
         androidResources = false
      }
   }

   compileOptions {
      // Android still creates java tasks, even with 100% Kotlin.
      // Ensure that target compatiblity is equal to kotlin's jvmToolchain
      lateinit var javaVersion: JavaVersion
      the<KotlinProjectExtension>().jvmToolchain { javaVersion = this.languageVersion.get().asJavaVersion() }

      targetCompatibility = javaVersion
   }

   buildTypes {
      debug {
         testCoverage {
            jacocoVersion = libs.versions.jacoco.get()
         }

         enableUnitTestCoverage = true
         enableAndroidTestCoverage = true
      }
   }
}

project.setupJacocoMergingAndroid()

dependencies {
   add("coreLibraryDesugaring", libs.desugarJdkLibs)
   add("detektPlugins", project(":detekt"))
}

configure<DetektExtension> {
   config.from("$rootDir/config/detekt-android.yml")
}

val runDebugTestsTask = tasks.register("runDebugTests")
val runDebugDetektTask = tasks.register("runDebugDetekt")

commonAndroidComponents {
   onVariants { variant ->
      // For variants, you can add extra filters, such as
      // && (variant.productFlavors.isEmpty() || variant.productFlavors.contains("version" to "develop"))
      if (variant.buildType == "debug") {

         if (!pluginManager.hasPlugin("com.android.test")) {
            runDebugTestsTask.dependsOn(variant.computeTaskName("test", "UnitTest"))

            runDebugDetektTask.dependsOn(variant.computeTaskName("detekt", "UnitTest"))
            runDebugDetektTask.dependsOn(variant.computeTaskName("detekt", "AndroidTest"))
         }
         runDebugDetektTask.dependsOn("detekt${variant.name.replaceFirstChar { it.uppercaseChar() }}")
      }
   }
}

// Even empty android test tasks take a while to execute. Disable all of them by default.
@Suppress("ComplexCondition") // It is just a properly commented list of tasks
tasks.configureEach {
   if (!customConfig.enableEmulatorTests.getOrElse(false) &&
      name.contains("AndroidTest", ignoreCase = true) &&
      !javaClass.name.startsWith("com.autonomousapps") && // https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/945
      !name.contains("Lint", ignoreCase = true) // Android lint does not like disabling their tasks
   ) {
      enabled = false
   }
}
