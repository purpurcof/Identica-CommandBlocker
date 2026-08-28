plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    jvmToolchain(25)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://registry.whereareiam.me/maven/packages")
}

dependencies {
    implementation(libs.shadow.gradle.plugin)
}
