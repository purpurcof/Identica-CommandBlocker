rootProject.name = "build-logic"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://registry.whereareiam.me/release")
        maven("https://registry.whereareiam.me/development")
    }
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://registry.whereareiam.me/release")
        maven("https://registry.whereareiam.me/development")
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
