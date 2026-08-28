rootProject.name = "Identica-CommandBlocker"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://registry.whereareiam.me/release")
        maven("https://registry.whereareiam.me/development")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        maven("https://registry.whereareiam.me/release")
        maven("https://registry.whereareiam.me/development")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

include(":api")
include(":common")
include(":velocity")
include(":bungeecord")

includeBuild("build-logic")
