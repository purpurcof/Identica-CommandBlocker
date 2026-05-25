plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.0" apply false
}

group = "me.purpurcof.identica"
version = "1.0.0"

allprojects {
    version = rootProject.version
    group = rootProject.group
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}