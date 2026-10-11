plugins {
    id("platform-conventions")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
    implementation(libs.attache.velocity)

    testImplementation(libs.velocity)
    testImplementation(libs.configura)
}

// the plugin version comes from the Gradle project version (see src/main/templates)
val generateBuildConstants = tasks.register<Copy>("generateBuildConstants") {
    val properties = mapOf("version" to project.version.toString())
    inputs.properties(properties)
    from("src/main/templates")
    into(layout.buildDirectory.dir("generated/sources/buildConstants"))
    expand(properties)
}

extensions.configure<JavaPluginExtension> {
    sourceSets["main"].java.srcDir(generateBuildConstants.map { it.destinationDir })
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("Identica-CommandBlocker-Velocity-${project.version}.jar")
}