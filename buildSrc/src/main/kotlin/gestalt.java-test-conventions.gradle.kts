/*
 * Apply the plugin to setup testing dependencies and tasks.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */

plugins {
    id("gestalt.java-common-conventions")
    `jvm-test-suite`
    id("org.gradle.test-retry")
    jacoco
}

dependencies {
    // Use JUnit Jupiter API for testing.
    testImplementation(libs.junit.api)

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly(libs.junit.engine)

    testImplementation(libs.assertJ)

    testImplementation(libs.mockito)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit5.get())
            targets {
                all {
                    testTask.configure {
                        finalizedBy(tasks.jacocoTestReport)

                        retry {
                            maxRetries = 3
                            maxFailures = 10
                            failOnPassedAfterRetry = false
                            failOnSkippedAfterRetry = false
                        }
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
