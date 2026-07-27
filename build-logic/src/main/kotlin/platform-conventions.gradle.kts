plugins {
    id("java-conventions")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":common"))
}

tasks.shadowJar {
    minimize()
}