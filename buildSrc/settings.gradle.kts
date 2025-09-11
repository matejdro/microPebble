dependencyResolutionManagement {
   versionCatalogs {
      create("libs") {
         from(files("../config/libs.toml"))
      }
   }
}

rootProject.name = "buildSrc"
