package jacoco

import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.tasks.JacocoReport

fun Project.setupJacocoMergingAndroid() {
   registerJacocoConfigurations()

   if (project.name != "test") {
      artifacts {
         add(CONFIGURATION_JACOCO_SOURCES, layout.projectDirectory.dir("src/main/kotlin"))
         add(CONFIGURATION_JACOCO_CLASSES, layout.buildDirectory.dir("tmp/kotlin-classes/debug").map { it.asFile })
         add(CONFIGURATION_JACOCO_EXEC, layout.buildDirectory.dir("outputs/unit_test_code_coverage").map { it.asFile })
         add(CONFIGURATION_JACOCO_EXEC, layout.buildDirectory.dir("outputs/code_coverage").map { it.asFile })
      }
   }
}

fun Project.setupJacocoMergingPureKotlin() {
   registerJacocoConfigurations()

   if (project.name != "test") {
      artifacts {
         add(CONFIGURATION_JACOCO_SOURCES, layout.projectDirectory.dir("src/main/kotlin"))
         add(CONFIGURATION_JACOCO_CLASSES, layout.buildDirectory.dir("classes/kotlin/main").map { it.asFile })
         add(CONFIGURATION_JACOCO_EXEC, layout.buildDirectory.dir("jacoco").map { it.asFile })
      }
   }
}

private fun Project.registerJacocoConfigurations() {
   configurations.register(CONFIGURATION_JACOCO_SOURCES)
   configurations.register(CONFIGURATION_JACOCO_CLASSES)
   configurations.register(CONFIGURATION_JACOCO_EXEC)
}

@Suppress("UnstableApiUsage") // Isolated projects
fun Project.setupJacocoMergingRoot() {
   registerJacocoConfigurations()

   tasks.register("aggregatedJacocoReport", JacocoReport::class).apply {
      configure {
         classDirectories.from(
            configurations.getByName(CONFIGURATION_JACOCO_CLASSES).incoming.artifactView {
               isLenient = true
            }.files.map { classDirectory ->
               fileTree(classDirectory) {
                  // Exclude release classes
                  exclude("**/release/**")

                  // Exclude generated classes
                  exclude("**/*ComposableSingletons*")
                  exclude("**/*MetroFactory*/**")
                  exclude("**/*MetroGraph*/**")
                  exclude("**/metro/hints/**")
                  exclude("**/android/showkase/**")
                  exclude("**/*PreviewKt.class")

                  // DI
                  exclude("**/*Providers.class")
                  exclude("**/*Providers$*.class")
               }
            }
         )
         executionData.from(
            configurations.getByName(CONFIGURATION_JACOCO_EXEC).incoming.artifactView { isLenient = true }.files
               .map { execDirectory ->
                  fileTree(execDirectory) {
                     include("**/*.exec")
                     include("**/*.ec")
                  }
               }
         )
         sourceDirectories.from(
            configurations.getByName(CONFIGURATION_JACOCO_SOURCES).incoming.artifactView { isLenient = true }.files
         )

         reports.xml.required.set(true)
      }
   }

   loadJacocoPathsFromSubprojects()
}

private fun Project.loadJacocoPathsFromSubprojects() {
   val rootProject = this

   subprojects {
      rootProject.dependencies.add(
         CONFIGURATION_JACOCO_CLASSES,
         rootProject.dependencies.project(
            mapOf(
               "path" to isolated.path,
               "configuration" to CONFIGURATION_JACOCO_CLASSES
            )
         )
      )

      rootProject.dependencies.add(
         CONFIGURATION_JACOCO_SOURCES,
         rootProject.dependencies.project(
            mapOf(
               "path" to isolated.path,
               "configuration" to CONFIGURATION_JACOCO_SOURCES
            )
         )
      )

      rootProject.dependencies.add(
         CONFIGURATION_JACOCO_EXEC,
         rootProject.dependencies.project(
            mapOf(
               "path" to isolated.path,
               "configuration" to CONFIGURATION_JACOCO_EXEC
            )
         )
      )
   }
}

const val CONFIGURATION_JACOCO_SOURCES = "jacocoSources"
const val CONFIGURATION_JACOCO_CLASSES = "jacocoClasses"
const val CONFIGURATION_JACOCO_EXEC = "jacocoExec"
