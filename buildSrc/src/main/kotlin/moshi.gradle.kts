import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

val libs = the<LibrariesForLibs>()

// Apply moshi the old way as a workaround for the https://github.com/ZacSweers/MoshiX/issues/499
apply(plugin = "dev.zacsweers.moshix")

dependencies {
   add("implementation", libs.moshi)
}
