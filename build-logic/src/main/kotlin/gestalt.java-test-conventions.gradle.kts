/*
 * Apply the plugin to setup testing dependencies and tasks.
 *
 * @author <a href="mailto:colin.redmond@outlook.com">Colin Redmond (c) 2023.
 */

plugins {
    id("gestalt.java-common-conventions")
    jacoco
}

dependencies {
    // Use JUnit Jupiter API for testing.
    testImplementation(libs.junitAPI)

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly(libs.junitEngine)

    testImplementation(libs.assertJ)

    testImplementation(libs.mockito)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.test {
    // Use junit platform for unit tests
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
        html.required.set(true)
    }
}
