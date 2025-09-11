package tasks

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

@DisableCachingByDefault(because = "IO bound task")
abstract class DetectTooManyKotlinFilesTask : SourceTask() {
   @get:Input
   abstract val errorMessage: Property<(Int) -> String>

   @get:Input
   abstract val threshold: Property<Int>

   @TaskAction
   fun execute() {
      val fileCount = source.files.count()
      if (fileCount > threshold.get()) {
         throw AssertionError(errorMessage.get().invoke(fileCount))
      }
   }
}

fun Project.setupTooManyKotlinFilesTaskForApp() {
   setupTooManyKotlinFilesTask(20) { fileCount ->
      "App module has too many kotlin files ($fileCount). " +
         "This module is compiled for every build, so it should have as little code as possible. " +
         "Consider moving some code to other modules."
   }
}

fun Project.setupTooManyKotlinFilesTaskForCommon() {
   setupTooManyKotlinFilesTask(20) { fileCount ->
      "Common module has too many kotlin files ($fileCount). " +
         "Lots of modules depend on this module, so it should have as little code as possible. " +
         "Consider splitting it up."
   }
}

private fun Project.setupTooManyKotlinFilesTask(threshold: Int, message: (fileCount: Int) -> String) {
   tasks.register<DetectTooManyKotlinFilesTask>("detectTooManyFiles") {
      errorMessage.set(message)
      this.threshold.set(threshold)

      val kotlinExtension = project.extensions.getByType(KotlinSourceSetContainer::class.java)

      source(
         provider {
            kotlinExtension.sourceSets
               .flatMap { sourceSet ->
                  sourceSet.kotlin.srcDirs.filter {
                     // Filter out generated sources, such as KSP or Moshi sources and tests
                     val relativeFile = it.relativeTo(projectDir)
                     relativeFile.startsWith("src") &&
                        !it.path.contains("test", ignoreCase = true)
                  }
               }
         },
      )

      include("**/*.kt")
   }
}
