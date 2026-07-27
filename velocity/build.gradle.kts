plugins {
    id("platform-conventions")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
    implementation(libs.attache.velocity)

    testImplementation(libs.velocity)
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("Identica-CommandBlocker-Velocity-${project.version}.jar")
}