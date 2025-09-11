import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

dependencies {
   add("implementation", libs.kotlin.coroutines.test)
   add("implementation", libs.turbine)
}
