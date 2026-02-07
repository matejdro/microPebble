import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

inline val PluginDependenciesSpec.commonAndroid: PluginDependencySpec
   get() = id("android-module-commons")

inline val PluginDependenciesSpec.compose: PluginDependencySpec
   get() = id("compose")

inline val PluginDependenciesSpec.di: PluginDependencySpec
   get() = id("di")

inline val PluginDependenciesSpec.androidAppModule: PluginDependencySpec
   get() = id("app-module")

inline val PluginDependenciesSpec.androidLibraryModule: PluginDependencySpec
   get() = id("library-android-module")

inline val PluginDependenciesSpec.pureKotlinModule: PluginDependencySpec
   get() = id("pure-kotlin-module")

inline val PluginDependenciesSpec.parcelize: PluginDependencySpec
   get() = id("kotlin-parcelize")

inline val PluginDependenciesSpec.moshi: PluginDependencySpec
   get() = id("moshi")

inline val PluginDependenciesSpec.navigation: PluginDependencySpec
   get() = id("navigation")

inline val PluginDependenciesSpec.testHelpers: PluginDependencySpec
   get() = id("test-module")
