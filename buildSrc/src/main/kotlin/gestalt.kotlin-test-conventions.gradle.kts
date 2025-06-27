/*
 * Apply the plugin to setup kotlin code test plugins.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */

plugins {
    id("gestalt.kotlin-common-conventions")
    `jvm-test-suite`
    id("org.gradle.test-retry")
    jacoco
}

dependencies {
    //Testing dependencies
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(libs.mockk)
    testImplementation(libs.koTestAssertions)
    testImplementation(libs.mockito)
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

//setup Jacoco
apply(plugin = "jacoco")

tasks.withType<JacocoReport> {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}


