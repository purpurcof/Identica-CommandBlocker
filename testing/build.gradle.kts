import java.time.Duration

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

dependencies {
    anvilRuntimeOnly(libs.anvil.protocol.mcprotocol)

    anvilImplementation(libs.junit.jupiter)
    anvilRuntimeOnly(libs.junit.platform)
}

/**
 * Identica's proxy plugin and its Credential provider are not Maven artifacts, so the test run is pointed at the
 * packaged jars: `-Pidentica.jar=<file>` / `IDENTICA_JAR`, and `-Pidentica.credential.jar=<file>` /
 * `IDENTICA_CREDENTIAL_JAR`. Use a build that satisfies the `identica` version in gradle/libs.versions.toml.
 */
fun suppliedJar(property: String, environment: String): File? =
    (providers.gradleProperty(property).orNull ?: providers.environmentVariable(environment).orNull)
        ?.let { file(it) }

val identicaJar = suppliedJar("identica.jar", "IDENTICA_JAR")
val credentialJar = suppliedJar("identica.credential.jar", "IDENTICA_CREDENTIAL_JAR")

/** The packaged (shaded) jar of this addon's Velocity plugin, built before the tests that install it. */
val velocityJar: Configuration = configurations.detachedConfiguration(
    dependencies.project(mapOf("path" to ":velocity", "configuration" to "shadowRuntimeElements"))
).apply { isTransitive = false }

anvil {
    acceptEula()
    engine {
        protocolLibrary("mcprotocol")
        processors.set(4)
        startupTimeout.set(Duration.ofMinutes(5))
    }

    artifact("commandblocker", velocityJar)
    if (identicaJar != null) artifact("identica", identicaJar)
    if (credentialJar != null) artifact("credential", credentialJar)
}

tasks.named("anvilTest") {
    doFirst {
        if (identicaJar == null || credentialJar == null)
            throw GradleException(
                "The live tests need Identica's jars: pass -Pidentica.jar=<file> and -Pidentica.credential.jar=<file> " +
                    "(or set IDENTICA_JAR and IDENTICA_CREDENTIAL_JAR)."
            )
    }
}
