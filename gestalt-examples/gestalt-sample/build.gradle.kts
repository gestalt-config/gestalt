plugins {
    id("gestalt.java-common-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
    `jvm-test-suite`
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(libs.testcontainers.vault)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            targets {
                all {
                    testTask.configure {
                        options {
                            val junitOptions = this as JUnitPlatformOptions;
                            junitOptions.excludeTags = setOf("cloud")
                        }
                    }
                }
            }

            testType.set(TestSuiteType.UNIT_TEST)
            dependencies {
                implementation(project(":gestalt-aws"))
                implementation(project(":gestalt-azure"))
                implementation(project(":gestalt-core"))
                implementation(project(":gestalt-git"))
                implementation(project(":gestalt-google-cloud"))
                implementation(project(":gestalt-hocon"))
                implementation(project(":gestalt-kotlin"))
                implementation(project(":gestalt-micrometer"))
                implementation(project(":gestalt-json"))
                implementation(project(":gestalt-toml"))
                implementation(project(":gestalt-validator-hibernate"))
                implementation(project(":gestalt-vault"))
                implementation(project(":gestalt-yaml"))


                implementation(project(":gestalt-kodein-di"))

                implementation(project(":gestalt-koin-di"))

                implementation(libs.aws.mock)

                implementation(project(":gestalt-guice"))
                implementation(libs.testcontainers.junit5)
                implementation(libs.micrometer)

                implementation(libs.hibernate.validator)

                implementation(libs.expressly)
                implementation(libs.jakarta.annotation)
            }
        }
    }
}


tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.github.gestalt.config.integration")
    }
}

