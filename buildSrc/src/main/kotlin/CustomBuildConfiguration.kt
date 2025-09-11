import org.gradle.api.provider.Property

interface CustomBuildConfiguration {
   val enableEmulatorTests: Property<Boolean>
}
