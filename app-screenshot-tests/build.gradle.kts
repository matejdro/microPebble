plugins {
   androidLibraryModule
   compose
   alias(libs.plugins.paparazzi)
}

android {
   namespace = "com.matejdro.micropebble.screenshottests"

   buildFeatures {
      androidResources = true
   }

   testOptions {
      unitTests.all {
         it.useJUnit()

         it.maxParallelForks = 1 // minOf(Runtime.getRuntime().availableProcessors(), 2)
      }
   }
}

plugins.withId("app.cash.paparazzi") {
   // Defer until afterEvaluate so that testImplementation is created by Android plugin.
   afterEvaluate {
      dependencies.constraints {
         add("testImplementation", "com.google.guava:guava") {
            attributes {
               attribute(
                  TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                  objects.named(TargetJvmEnvironment::class, TargetJvmEnvironment.STANDARD_JVM)
               )
            }
            because(
               "LayoutLib and sdk-common depend on Guava's -jre published variant." +
                  "See https://github.com/cashapp/paparazzi/issues/906."
            )
         }
      }
   }
}

dependencyAnalysis {
   issues {
      onIncorrectConfiguration {
         // screenshot tests need to include app as implementation, otherwise resources do not work properly
         exclude(":app")
      }

      onModuleStructure {
         // False positive
         severity("ignore")
      }
   }
}

dependencies {
   implementation(projects.app) {
      // If your app has multiple flavors, this is how you define them:
      //      attributes {
      //         attribute(
      //            ProductFlavorAttr.of("app"),
      //            objects.named(ProductFlavorAttr::class.java, "develop")
      //         )
      //      }
   }
   testImplementation(libs.junit4)
   testImplementation(libs.junit4.parameterinjector)
   testImplementation(libs.showkase)
}
