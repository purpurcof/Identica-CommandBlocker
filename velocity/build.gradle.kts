plugins {
    id("com.gradleup.shadow")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    compileOnly("me.whereareiam.identica:api:unspecified")
    compileOnly("me.whereareiam:keystone:dev-90d39d2")
    compileOnly("net.kyori:adventure-api:4.25.0")
    compileOnly("org.jetbrains:annotations:26.0.1")
    implementation(project(":common"))

    testImplementation("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    testImplementation("me.whereareiam.identica:api:unspecified")
    testImplementation("me.whereareiam:keystone:dev-90d39d2")
    testImplementation("net.kyori:adventure-api:4.25.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testImplementation("org.junit.platform:junit-platform-launcher:1.12.2")
    testImplementation("org.mockito:mockito-core:5.20.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("Identica-CommandBlocker-Velocity-${project.version}.jar")
    minimize()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}