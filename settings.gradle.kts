rootProject.name = "Identica-CommandBlocker"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://registry.whereareiam.me/maven/packages")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        maven("https://registry.whereareiam.me/maven/packages")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.opencollab.dev/main/")
        maven("https://repo.opencollab.dev/maven-snapshots/")
    }
}

include(":api")
include(":common")
include(":velocity")
include(":bungeecord")
include(":testing")

includeBuild("build-logic")
