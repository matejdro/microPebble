import com.android.build.gradle.internal.lint.AndroidLintTask

plugins {
   id("com.android.library")
   id("kotlinova")

   // Do not include android-library-module because this module does not need kotlin
   // It is purely resource-only, no code
}

android {
   compileSdk = 36

   namespace = "com.matejdro.micropebble.sharedresources"

   defaultConfig {
      minSdk = 30
   }

   buildFeatures {
      androidResources = true
   }

   lint {
      abortOnError = true
      warningsAsErrors = true
      lintConfig = file("$rootDir/config/android-lint.xml")
      baseline = file("lint-baseline.xml")
      sarifReport = true
   }
}

// Even empty android test tasks take a while to execute. Disable all of them by default.
tasks.configureEach {
   if (
      name.contains("test", ignoreCase = true) &&
      !javaClass.name.startsWith("com.autonomousapps") && // https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/945
      !name.contains("Lint", ignoreCase = true) // Android lint does not like disabling their tasks
   )
      enabled = false
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

kotlinova {
   mergeAndroidLintSarif = true
}

dependencies {
   // Intentionally blank. Do not put anything there.
}
