import me.whereareiam.attache.plugin.gradle.extension.AttacheExtension

plugins {
    id("java-conventions")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(libs.configura)
    compileOnly(libs.adventure.minimessage)
    implementation(libs.attache.standalone)
    attache(libs.configura)

    testImplementation(libs.configura)
    testImplementation(libs.adventure.minimessage)
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
