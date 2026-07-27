plugins {
    `java-library`
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    compileOnly(libs.findLibrary("identica-api").get())
    compileOnly(libs.findLibrary("keystone").get())
    compileOnly(libs.findLibrary("guice").get())
    compileOnly(libs.findLibrary("adventure").get())
    compileOnly(libs.findLibrary("annotations").get())
    compileOnly(libs.findLibrary("lombok").get())
    annotationProcessor(libs.findLibrary("lombok").get())

    testImplementation(libs.findLibrary("identica-api").get())
    testImplementation(libs.findLibrary("keystone").get())
    testImplementation(libs.findLibrary("guice").get())
    testImplementation(libs.findLibrary("adventure").get())
    testImplementation(libs.findLibrary("annotations").get())
    testCompileOnly(libs.findLibrary("lombok").get())
    testAnnotationProcessor(libs.findLibrary("lombok").get())
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testImplementation(libs.findLibrary("junit-platform").get())
    testImplementation(libs.findLibrary("mockito-core").get())
    testImplementation(libs.findLibrary("mockito-junit").get())
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}