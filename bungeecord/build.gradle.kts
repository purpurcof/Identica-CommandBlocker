plugins {
    id("platform-conventions")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(libs.bungeecord)
    implementation(libs.attache.bungeecord)

    testImplementation(libs.bungeecord)
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("Identica-CommandBlocker-BungeeCord-${project.version}.jar")
}
