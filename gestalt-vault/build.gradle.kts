plugins {
    id("gestalt.java-library-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.java-code-quality-conventions")
    id("gestalt.java-publish-conventions")
}

dependencies {
    implementation(project(":gestalt-core"))
    api(libs.vault)

    testImplementation(libs.testcontainers.junit5)
    testImplementation(libs.testcontainers.vault)
}


