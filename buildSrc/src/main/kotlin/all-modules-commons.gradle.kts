import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import tasks.setupTooManyKotlinFilesTaskForCommon
import util.commonKotlinCompilerOptions

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

commonKotlinCompilerOptions {
   optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
   optIn.add("kotlinx.coroutines.FlowPreview")
   optIn.add("com.google.accompanist.permissions.ExperimentalPermissionsApi")
   optIn.add("kotlin.time.ExperimentalTime")
   optIn.add("kotlin.uuid.ExperimentalUuidApi")
   optIn.add("kotlin.ExperimentalUnsignedTypes")

   // https://blog.jetbrains.com/idea/2025/09/improved-annotation-handling-in-kotlin-2-2-less-boilerplate-fewer-surprises/
   freeCompilerArgs.add("-Xannotation-default-target=param-property")
}

dependencies {
   if (configurations.findByName("testImplementation") != null) {
      add("testImplementation", libs.junit.api)
      add("testImplementation", libs.kotest.assertions)
      add("testImplementation", libs.kotlin.coroutines.test)
      add("testImplementation", libs.turbine)

      add("testRuntimeOnly", libs.junit.engine)
      add("testRuntimeOnly", libs.junit.launcher)
   }
}
