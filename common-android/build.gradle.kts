plugins {
   androidLibraryModule
}

dependencyAnalysis {
   issues {
      onModuleStructure {
         // Yes, this technically does not have any Android code, but it serves as an example, so it needs to remain Android
         severity("ignore")
      }
   }
}

dependencies {
}
