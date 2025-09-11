plugins {
   androidLibraryModule
   compose
   di
}

android {
    namespace = "com.matejdro.micropebble.${NAME}.ui"

    buildFeatures {
        androidResources = true
    }
}

dependencies {
    api(projects.${NAME}.api)

    testImplementation(projects.common.test)
}
