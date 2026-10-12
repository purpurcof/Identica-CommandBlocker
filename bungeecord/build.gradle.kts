plugins {
    id("platform-conventions")
}

dependencies {
    compileOnly(libs.bungeecord)
    compileOnly(libs.configura)

    testImplementation(libs.bungeecord)
    testImplementation(libs.configura)
}

tasks.processResources {
    val properties = mapOf("version" to project.version.toString())
    inputs.properties(properties)
    filesMatching("bungee.yml") { expand(properties) }
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("Identica-CommandBlocker-BungeeCord-${project.version}.jar")
}