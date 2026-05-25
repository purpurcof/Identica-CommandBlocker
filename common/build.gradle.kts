dependencies {
    compileOnly(libs.identica.api)
    compileOnly(libs.keystone)
    implementation(libs.configura)
    compileOnly(libs.adventure)
    compileOnly(libs.adventure.minimessage)
    compileOnly(libs.annotations)

    testImplementation(libs.identica.api)
    testImplementation(libs.keystone)
    testImplementation(libs.configura)
    testImplementation(libs.adventure)
    testImplementation(libs.adventure.minimessage)
    testImplementation(libs.annotations)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
