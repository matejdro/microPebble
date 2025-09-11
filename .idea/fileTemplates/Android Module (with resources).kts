plugins {
   androidLibraryModule
   di
}

android {

    namespace = "com.matejdro.micropebble.${NAME}"
    buildFeatures {
        androidResources = true
    }
}

dependencies {
    testImplementation(projects.common.test)
}
