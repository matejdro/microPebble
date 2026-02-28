import com.android.build.gradle.internal.lint.AndroidLintTask
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.accessors.dm.LibrariesForLibs
import si.inova.kotlinova.gradle.KotlinovaExtension
import util.commonAndroid
import util.isAndroidProject

val libs = the<LibrariesForLibs>()

plugins {
   id("kotlinova")
}

// Apply detekt the old way until https://github.com/detekt/detekt/issues/8977 is solved
apply(plugin = "dev.detekt")

if (isAndroidProject()) {
   commonAndroid {
      lint {
         lintConfig = file("$rootDir/config/android-lint.xml")
         abortOnError = true

         warningsAsErrors = true
         sarifReport = true
      }
   }

   tasks.withType(AndroidLintTask::class.java).configureEach {
      if (this.name.contains("Baseline") || this.name.startsWith("lintVital")) {
         // Baseline tasks and vital lint tasks do not expose sarif files
         return@configureEach
      }

      // Workaround for the https://github.com/detekt/sarif4k/issues/220
      doLast {
         val sarifFile = sarifReportOutputFile.get().asFile
         val sarifFileText = sarifFile.readText()

         val fixedSarifText = sarifFileText.replace(
            "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
            "https://docs.oasis-open.org/sarif/sarif/v2.1.0/errata01/os/schemas/sarif-schema-2.1.0.json"
         )

         sarifFile.writeText(fixedSarifText)
      }
   }

   dependencies {
      add("lintChecks", (libs.android.securityLints))
   }
}

configure<DetektExtension> {
   config.setFrom("$rootDir/config/detekt.yml")
}

tasks.withType<Detekt>() {
   val buildDir = project.layout.buildDirectory.asFile.get().absolutePath
   // Exclude all generated files
   exclude {
      it.file.absolutePath.contains(buildDir)
   }
}

configure<KotlinovaExtension> {
   mergeDetektSarif = true
   if (isAndroidProject()) {
      mergeAndroidLintSarif = true
   }

   enableDetektPreCommitHook = true
}

dependencies {
   add("detektPlugins", libs.detekt.ktlint)
   add("detektPlugins", libs.detekt.compose)
   add("detektPlugins", libs.kotlinova.navigation.detekt)
}
