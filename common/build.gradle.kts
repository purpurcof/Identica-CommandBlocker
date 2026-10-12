plugins {
    id("java-conventions")
}

dependencies {
    // Provided by Identica at runtime: the settings are read through IdenticaAPI.getConfigura(), so this
    // addon does not load its own copy. Keep the version in line with the one the targeted Identica ships.
    compileOnly(libs.configura)
    compileOnly(libs.adventure.minimessage)
    api(project(":api"))

    testImplementation(libs.configura)
    testImplementation(libs.adventure.minimessage)
}
