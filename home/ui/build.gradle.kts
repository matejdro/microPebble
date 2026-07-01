plugins {
   kmpLibraryModule
   id("dev.zacsweers.metro")
   id("com.google.devtools.ksp")
   id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
   androidLibrary {
      namespace = "com.matejdro.micropebble.home.ui"
      androidResources.enable = true
   }

   sourceSets {
      commonMain.dependencies {
         // api: HomeScreenContent's public signature exposes common-navigation's Tab / SelectedTabContent.
         api(projects.commonNavigation)
         implementation(libs.compose.foundation)
         implementation(libs.compose.material3)
         implementation(libs.compose.animation)
         implementation(libs.compose.ui)
         implementation(libs.compose.components.resources)
      }

      androidMain {
         languageSettings.optIn("com.google.accompanist.permissions.ExperimentalPermissionsApi")

         dependencies {
            api(projects.notification.api)
            api(projects.common)
            api(projects.logging.api)
            api(projects.voice.api)
            api(libs.androidx.core)
            api(libs.kotlinova.core)
            api(libs.kotlinova.navigation)

            implementation(projects.sharedResources)
            implementation(projects.commonCompose)
            implementation(libs.accompanist.permissions)
            implementation(libs.composePreference)
            implementation(libs.kotlin.coroutines)
            implementation(libs.kotlin.serialization.core)
            implementation(libs.dispatch)
            implementation(libs.androidx.compose.material3.sizeClasses)
            implementation(libs.libpebble3)
            implementation(libs.showkase)

            implementation(libs.androidx.compose.ui)
            implementation(libs.androidx.compose.ui.graphics)
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.material3)
            implementation(libs.androidx.lifecycle.compose)
            implementation(libs.kotlinova.compose)
         }
      }
   }
}

val stableClassesFile = project.layout.settingsDirectory.file("config/global_compose_stable_classes.txt")
composeCompiler {
   stabilityConfigurationFiles.add(stableClassesFile)
}

compose.resources {
   packageOfResClass = "com.matejdro.micropebble.home.resources"
}

ksp {
   arg("skipPrivatePreviews", "true")
}

dependencies {
   add("kspAndroid", libs.showkase.processor)
   add("kspAndroid", libs.kotlinova.navigation.compiler)
}
