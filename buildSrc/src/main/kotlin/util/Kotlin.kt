package util

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

fun Project.commonKotlinCompilerOptions(action: KotlinJvmCompilerOptions.() -> Unit) {
   if (isAndroidProject()) {
      configure<KotlinAndroidProjectExtension> {
         compilerOptions(action)
      }
   } else {
      configure<KotlinJvmProjectExtension> {
         compilerOptions(action)
      }
   }
}
