dependencies {
    compileOnly("me.whereareiam.identica:api:unspecified")
    compileOnly("me.whereareiam:keystone:dev-90d39d2")
    implementation("me.whereareiam:configura:dev-6b221cb")
    compileOnly("net.kyori:adventure-api:4.25.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
    compileOnly("org.jetbrains:annotations:26.0.1")

    testImplementation("me.whereareiam.identica:api:unspecified")
    testImplementation("me.whereareiam:keystone:dev-90d39d2")
    testImplementation("me.whereareiam:configura:dev-6b221cb")
    testImplementation("net.kyori:adventure-api:4.25.0")
    testImplementation("net.kyori:adventure-text-minimessage:4.25.0")
    testImplementation("org.jetbrains:annotations:26.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testImplementation("org.junit.platform:junit-platform-launcher:1.12.2")
    testImplementation("org.mockito:mockito-core:5.20.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}