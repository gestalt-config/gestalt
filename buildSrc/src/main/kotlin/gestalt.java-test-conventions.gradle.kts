import org.github.gestalt.config.Test

/*
 * Apply the plugin to setup testing dependencies and tasks.
 */

plugins {
    id("gestalt.java-common-conventions")
    jacoco
}

dependencies {
    // Use JUnit Jupiter API for testing.
    testImplementation(Test.junitAPI)

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly(Test.junitEngine)

    testImplementation(Test.assertJ)

    testImplementation(Test.mockito)
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
