import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import tasks.setupTooManyKotlinFilesTaskForCommon

val libs = the<LibrariesForLibs>()

plugins {
   id("checks")
   id("dependency-analysis")
}

configure<KotlinProjectExtension> {
   jvmToolchain(21)
}

if (name.startsWith("common-")) {
   setupTooManyKotlinFilesTaskForCommon()
}

dependencies {
   if (configurations.findByName("testImplementation") != null) {
      add("testImplementation", libs.junit5.api)
      add("testImplementation", libs.kotest.assertions)
      add("testImplementation", libs.kotlin.coroutines.test)
      add("testImplementation", libs.turbine)

      add("testRuntimeOnly", libs.junit5.engine)
      add("testRuntimeOnly", libs.junit5.launcher)
   }
}
