plugins {
    id("com.gradleup.shadow")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(libs.bungeecord)
    compileOnly(libs.identica.api)
    compileOnly(libs.keystone)
    compileOnly(libs.guice)
    compileOnly(libs.adventure)
    compileOnly(libs.annotations)
    implementation(project(":common"))
    implementation(libs.attache.bungeecord)

    testImplementation(libs.bungeecord)
    testImplementation(libs.identica.api)
    testImplementation(libs.keystone)
    testImplementation(libs.guice)
    testImplementation(libs.adventure)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)
}
tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("Identica-CommandBlocker-BungeeCord-${project.version}.jar")
    minimize()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
