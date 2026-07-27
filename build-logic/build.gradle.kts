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
    maven("https://maven.whereareiam.me/release")
    maven("https://maven.whereareiam.me/development")
}

dependencies {
    implementation(libs.shadow.gradle.plugin)
}