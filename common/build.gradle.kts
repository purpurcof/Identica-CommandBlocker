import me.whereareiam.attache.plugin.gradle.extension.AttacheExtension

plugins {
    `java-library`
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(libs.identica.api)
    compileOnly(libs.keystone)
    compileOnly(libs.configura)
    compileOnly(libs.adventure)
    compileOnly(libs.adventure.minimessage)
    compileOnly(libs.annotations)
    implementation(libs.attache.standalone)
    attache(libs.configura)

    testImplementation(libs.identica.api)
    testImplementation(libs.keystone)
    testImplementation(libs.configura)
    testImplementation(libs.adventure)
    testImplementation(libs.adventure.minimessage)
    testImplementation(libs.annotations)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)
}

extensions.configure<AttacheExtension>("attache") {
    transitive.set(true)
    repository("https://maven.whereareiam.me/release")
    repository("https://maven.whereareiam.me/development")
    library(libs.configura) {
        relocate("com{}fasterxml{}jackson", "me.purpurcof.identica.addon.commandblocker.libs.jackson")
        relocate("org{}yaml{}snakeyaml", "me.purpurcof.identica.addon.commandblocker.libs.snakeyaml")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
