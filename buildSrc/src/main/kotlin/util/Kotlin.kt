package util

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.commonKotlinCompilerOptions(action: KotlinCommonCompilerOptions.() -> Unit) {
   when {
      pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {
         configure<KotlinMultiplatformExtension> {
            compilerOptions(action)
         }
      }
      isAndroidProject() -> {
         configure<KotlinAndroidProjectExtension> {
            compilerOptions(action)
         }
      }
      else -> {
         configure<KotlinJvmProjectExtension> {
            compilerOptions(action)
         }
      }
   }
}
