plugins {
    id("gestalt.java-library-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.java-code-quality-conventions")
    id("gestalt.java-publish-conventions")
}

dependencies {
    implementation(project(":gestalt-core"))
    api(platform(libs.azure.bom))
    api(libs.azure.blob)
    api(libs.azure.identity)
    api(libs.azure.secret)

    testImplementation(libs.testcontainers.junit5)
}


