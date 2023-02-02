plugins {
    id("gestalt.java-library-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.java-code-quality-conventions")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaLatest.get()))
    }
}
dependencies {
    testImplementation(project(":gestalt-core"))
    testImplementation(libs.slf4j.simple)
}
