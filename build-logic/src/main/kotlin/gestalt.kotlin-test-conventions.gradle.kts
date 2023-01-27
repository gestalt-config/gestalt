/*
 * Apply the plugin to setup kotlin code test plugins.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */

plugins {
    id("gestalt.kotlin-common-conventions")
    jacoco
}

dependencies {
    //Testing dependencies
    testImplementation(libs.junitAPI)
    testRuntimeOnly(libs.junitEngine)
    testImplementation(libs.mockk)
    testImplementation(libs.koTestAssertions)
    testImplementation(libs.mockito)
}

tasks.test {
    // Use junit platform for unit tests
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

//setup Jacoco
apply(plugin = "jacoco")

tasks.withType<JacocoReport> {
    reports {
        xml.required.set(false)
        html.required.set(false)
    }
}


