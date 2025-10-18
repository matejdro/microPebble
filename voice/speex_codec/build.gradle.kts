plugins {
   androidLibraryModule
}

android {
   defaultConfig {
      externalNativeBuild {
         cmake {
            cppFlags("")
         }
      }
   }

   ndkVersion = "29.0.14206865"

   externalNativeBuild {
      cmake {
         path("src/main/cpp/CMakeLists.txt")
         version = "3.22.1"
      }
   }
}

dependencyAnalysis {
   issues {
      onModuleStructure {
         // False positive
         severity("ignore")
      }
   }
}
