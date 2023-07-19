plugins {
    id("gestalt.java-library-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.java-code-quality-conventions")
    id("gestalt.java-publish-conventions")
}

dependencies {
    implementation(project(":gestalt-core"))
    implementation(platform(libs.aws.bom))
    api(libs.aws.s3)
    api(libs.aws.secret)
    api(libs.aws.url.client)
    implementation(libs.jackson.databind)
    testImplementation(libs.aws.mock)
    testImplementation(libs.testcontainers.junit5)
}


