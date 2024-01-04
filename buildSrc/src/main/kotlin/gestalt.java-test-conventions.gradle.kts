/*
 * Apply the plugin to setup testing dependencies and tasks.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */

plugins {
    id("gestalt.java-common-conventions")
    `jvm-test-suite`
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

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            targets {
                all {
                    testTask {
                        finalizedBy(tasks.jacocoTestReport)
                    }
                }
            }
        }
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
