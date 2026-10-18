import me.whereareiam.anvil.integration.junit.gradle.AnvilTest
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    java
    alias(libs.plugins.anvil)
    alias(libs.plugins.anvil.junit)
    alias(libs.plugins.anvil.capability.default)
    alias(libs.plugins.anvil.platform.paper)
    alias(libs.plugins.anvil.platform.velocity)
}

description = "Live player journeys against the packaged Identica-CommandBlocker on a real Velocity proxy"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

val identicaRelease = libs.versions.identica.get()

/** Resolved once per artifact name, so the download task below is registered once per artifact. */
val identicaJars = mutableMapOf<String, Any>()

/**
 * A jar of Identica's release `identica` in gradle/libs.versions.toml, which the release attaches on GitHub and
 * which is downloaded once into the build directory; or the file given as a Gradle property, to run against a build of
 * your own, such as `-Pidentica.jar=<file>` for the plugin.
 */
fun identicaJar(name: String, property: String): Any = identicaJars.getOrPut(name) {
    providers.gradleProperty(property).orNull?.let { return@getOrPut file(it) }

    val fileName = "Identica-$name-$identicaRelease.jar"
    val target = layout.buildDirectory.file("identica/$fileName")
    val url = "https://github.com/whereareiam/Identica/releases/download/$identicaRelease/$fileName"
    val download = tasks.register("downloadIdentica$name") {
        description = "Downloads $fileName from Identica's release $identicaRelease."
        outputs.file(target)
        doLast {
            val jar = target.get().asFile
            if (jar.isFile) return@doLast

            jar.parentFile.mkdirs()
            // A download that stops halfway must not leave a jar the next run takes for complete.
            val partial = jar.resolveSibling("$fileName.part")
            URI(url).toURL().openStream().use { input ->
                Files.copy(input, partial.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            Files.move(partial.toPath(), jar.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
    files(target).builtBy(download)
}

dependencies {
    // The models of the Identica release the tests run against, which the addon is built against as well, to read the
    // messages its proxy generated through them.
    anvilImplementation(libs.identica.api)
    // Credential's message models, to wait for the prompts its registration steps announce before reading a command.
    anvilImplementation(identicaJar("Credential", "identica.credential.jar"))
    // Identica's API declares the Commandant models its messages are made of as compileOnly; reading them needs them.
    anvilImplementation(libs.commandant)
    anvilImplementation(libs.configura)
    // The addon's own settings model, to write its settings file and read its messages back through it.
    anvilImplementation(project(":common"))

    anvilImplementation(libs.junit.jupiter)

    anvilRuntimeOnly(libs.anvil.protocol.mcprotocol)
    anvilRuntimeOnly(libs.junit.platform)
}

/** The packaged (shaded) jar of this addon's Velocity plugin, built before the tests that install it. */
val velocityJar: Configuration = configurations.detachedConfiguration(
    dependencies.project(mapOf("path" to ":velocity", "configuration" to "shadowRuntimeElements"))
).apply { isTransitive = false }

anvil {
    acceptEula()
    engine {
        protocolLibrary("mcprotocol")
        // A server sizes its thread pools from the processors it sees; all of them at once stall the machine.
        processors.set(4)
        // Lets the test processes yield to whatever else the machine is doing. Windows has no nice.
        if (!System.getProperty("os.name").lowercase().contains("win")) processPriority.set("low")
    }

    artifact("identica", identicaJar("BUNDLE", "identica.jar"))
    artifact("credential", identicaJar("Credential", "identica.credential.jar"))
    artifact("premium", identicaJar("Premium", "identica.premium.jar"))
    artifact("commandblocker", velocityJar)
}

// Every managed process runs on one exact LTS chosen during planning. Asking for the JVM the tests themselves run
// on lets Anvil use that installed JDK instead of provisioning another one, which needs no download and no second
// Java installation on the machine or the runner.
tasks.withType<AnvilTest>().configureEach {
    systemProperty("anvil.java.version", "25")
}
