plugins {
    id("gestalt.java-library-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.kotlin-common-conventions")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaLatest.get()))
    }
}

dependencies {
    testImplementation(project(":gestalt-core"))
    testImplementation(libs.jakarta.annotation)
}
