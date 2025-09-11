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
      minSdk = 26
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

kotlinova {
   mergeAndroidLintSarif = true
}

dependencies {
   // Intentionally blank. Do not put anything there.
}
