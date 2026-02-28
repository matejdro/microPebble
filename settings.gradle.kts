pluginManagement {
   repositories {
      google()
      mavenCentral()
      gradlePluginPortal()
   }
}

dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

   repositories {
      mavenLocal()
      google()
      mavenCentral()
      maven("https://jitpack.io")
   }

   versionCatalogs {
      create("libs") {
         from(files("config/libs.toml"))
      }
   }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "MicroPebble"
includeBuild("libs/libpebble3") {
   dependencySubstitution {
      substitute(module("com.coredevices:libpebble3"))
         .using(project(":libpebble3"))
   }
}

include(":app")
include(":app-screenshot-tests")
include(":bluetooth:api")
include(":bluetooth:data")
include(":bluetooth:ui")
include(":apps:ui")
include(":appstore:api")
include(":appstore:data")
include(":appstore:ui")
include(":common")
include(":common:test")
include(":common-android")
include(":common-android:test")
include(":common-compose")
include(":common-navigation")
include(":detekt")
include(":home:ui")
include(":logging:api")
include(":logging:crashreport")
include(":logging:data")
include(":notification:api")
include(":notification:data")
include(":notification:ui")
include(":shared-resources")
include(":voice:api")
include(":voice:data")
include(":voice:speex_codec")
include(":webservices:api")
include(":webservices:data")
include(":webservices:ui")
