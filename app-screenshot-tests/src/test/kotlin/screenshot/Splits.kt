package screenshot

object Splits {
   val paparazziSplits = listOf<List<String>>(
      // List all your snapshot test packages here, split into N lists, where N is amount of paralelization you want
      // Then copy "Tests1" into N classes and rename it + edit SplitIndex inside the class
      // Finally, update number at the end of the "it.maxParallelForks = minOf" in the build.gradle.kts of this module
      // to the N
      throw NotImplementedError("Missing splits")
   )
}
