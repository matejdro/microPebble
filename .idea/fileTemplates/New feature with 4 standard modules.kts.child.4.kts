plugins {
   pureKotlinModule
   testHelpers
}

dependencies {
    api(projects.${NAME}.api)
    implementation(projects.common.test)    
}
