import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

val libs = the<LibrariesForLibs>()

plugins {
   id("kotlinx-serialization")
}

dependencies {
   add("implementation", libs.kotlin.serialization.core)
}
