import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("com.google.devtools.ksp")
}

dependencies {
   if (name != "common-navigation") {
      add("implementation", project(":common-navigation"))
   }

   ksp(libs.kotlinova.navigation.compiler)

   add("testImplementation", libs.kotlinova.navigation.test)
}
