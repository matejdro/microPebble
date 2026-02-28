import com.slack.keeper.optInToKeeper

plugins {
   androidAppModule
   compose
   navigation
   parcelize
   showkase
   id("com.slack.keeper")
   id("androidx.baselineprofile")
}

setProperty("archivesBaseName", "micropebble")

android {
   namespace = "com.matejdro.micropebble"

   buildFeatures {
      buildConfig = true
   }

   defaultConfig {
      applicationId = "com.matejdro.micropebble"
      targetSdk = 34
      versionCode = 1
      versionName = "1.0.0"

      testInstrumentationRunner = "com.matejdro.micropebble.instrumentation.TestRunner"
      testInstrumentationRunnerArguments += "clearPackageData" to "true"
      // Needed to enable test coverage
      testInstrumentationRunnerArguments += "useTestStorageService" to "true"
   }

   testOptions {
      execution = "ANDROIDX_TEST_ORCHESTRATOR"
   }

   if (hasProperty("testAppWithProguard")) {
      testBuildType = "proguardedDebug"
   }

   signingConfigs {
      getByName("debug") {
         // SHA1: 89:73:5C:0D:10:99:A1:05:79:86:D0:B5:5D:79:A7:23:BC:9C:1E:84
         // SHA256: E2:AE:A0:B7:F7:9C:2C:13:42:DC:72:63:D0:40:13:D2:18:63:37:00:1A:59:B6:C9:05:57:67:51:E3:D0:E3:C7

         storeFile = File(rootDir, "keys/debug.jks")
         storePassword = "android"
         keyAlias = "androiddebugkey"
         keyPassword = "android"
      }

      create("release") {
         // SHA1: 53:55:2F:22:3C:08:A8:75:EE:72:9E:F4:55:A2:0E:3B:01:05:4E:0D
         // SHA256: 2F:D0:DE:FE:32:38:06:E3:E3:8B:48:F3:1A:65:03:5F:D9:6B:93:CA:66:1B:32:D4:85:B6:CA:B5:AD:F5:17:0F

         storeFile = File(rootDir, "keys/release.jks")
         storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
         keyAlias = "app"
         keyPassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
      }
   }

   buildTypes {
      getByName("debug") {
         signingConfig = signingConfigs.getByName("debug")
      }

      create("proguardedDebug") {
         isMinifyEnabled = true
         isShrinkResources = true

         proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
         )

         testProguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
            "proguard-rules-test.pro"
         )

         matchingFallbacks += "debug"

         signingConfig = signingConfigs.getByName("debug")
      }

      create("benchmark") {
         isDebuggable = true
         initWith(buildTypes.getByName("release"))
         signingConfig = signingConfigs.getByName("debug")
         matchingFallbacks += listOf("release")
      }

      getByName("release") {
         isMinifyEnabled = true
         isShrinkResources = true

         proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
         )

         signingConfig = signingConfigs.getByName("release")
      }
   }
}

androidComponents {
   beforeVariants { builder ->
      if (builder.name.contains("proguardedDebug")) {
         builder.optInToKeeper()
      }
   }
}

keeper {
   automaticR8RepoManagement = false
}

custom {
   enableEmulatorTests.set(true)
}

dependencies {
   implementation(projects.common)
   implementation(libs.androidx.work.runtime)
   compileOnly(projects.commonAndroid)
   implementation(projects.apps.ui)
   implementation(projects.appstore.api)
   implementation(projects.appstore.data)
   implementation(projects.appstore.ui)
   implementation(projects.bluetooth.api)
   implementation(projects.bluetooth.data)
   implementation(projects.bluetooth.ui)
   implementation(projects.commonNavigation)
   implementation(projects.commonCompose)
   implementation(projects.notification.api)
   implementation(projects.notification.data)
   implementation(projects.notification.ui)
   implementation(projects.home.ui)
   implementation(projects.logging.api)
   implementation(projects.logging.crashreport)
   implementation(projects.logging.data)
   implementation(projects.sharedResources)
   implementation(projects.voice.api)
   implementation(projects.voice.data)
   implementation(projects.webservices.api)
   implementation(projects.webservices.data)
   implementation(projects.webservices.ui)

   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.androidx.core.splashscreen)
   implementation(libs.androidx.lifecycle.runtime)
   implementation(libs.androidx.lifecycle.viewModel)
   implementation(libs.androidx.lifecycle.viewModel.compose)
   implementation(libs.coil)
   implementation(libs.dispatch)
   implementation(libs.kable)
   implementation(libs.kotlin.immutableCollections)
   implementation(libs.libpebble3)
   implementation(libs.moshi)
   implementation(libs.kermit)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.navigation)
   implementation(libs.simpleStack)
   implementation(libs.tinylog.api)
   implementation(libs.tinylog.impl)

   implementation(libs.androidx.datastore)
   implementation(libs.androidx.datastore.preferences)

   runtimeOnly(libs.ktor.okhttp)

   keeperR8(libs.androidx.r8)
}
