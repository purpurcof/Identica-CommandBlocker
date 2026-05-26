group = "me.purpurcof.identica"
version = "1.1.0"

allprojects {
    version = rootProject.version
    group = rootProject.group
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}