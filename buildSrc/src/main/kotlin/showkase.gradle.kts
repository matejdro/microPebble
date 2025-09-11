import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("com.google.devtools.ksp")
}

ksp {
   arg("skipPrivatePreviews", "true")
}

dependencies {
   add("ksp", libs.showkase.processor)
   add("implementation", libs.showkase)
}
