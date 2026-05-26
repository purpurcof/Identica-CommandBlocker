plugins {
    `java-library`
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
