package util

import com.android.build.api.dsl.AndroidResources
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.Installation
import com.android.build.api.dsl.ProductFlavor
import com.android.build.gradle.internal.utils.KOTLIN_ANDROID_PLUGIN_ID
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * android {} block that can be used without applying specific android plugin
 */
fun Project.commonAndroid(
   block: Action<
      CommonExtension<
         BuildFeatures,
         BuildType,
         DefaultConfig,
         ProductFlavor,
         AndroidResources,
         Installation
         >
      >,
) {
   (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("android", block)
}

fun Project.isAndroidProject(): Boolean {
   return pluginManager.hasPlugin(KOTLIN_ANDROID_PLUGIN_ID)
}
