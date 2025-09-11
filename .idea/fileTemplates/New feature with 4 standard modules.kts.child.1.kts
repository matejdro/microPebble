plugins {
   pureKotlinModule
   di
}

dependencies {
    api(projects.${NAME}.api)
    
    testImplementation(projects.common.test)    
}
