plugins {
    id("gestalt.java-common-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
    `jvm-test-suite`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of("17"))
    }
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

            dependencies {
                implementation(project(":gestalt-core"))
                implementation(project(":gestalt-hocon"))
                implementation(project(":gestalt-kotlin"))
                implementation(project(":gestalt-json-jackson3"))
                implementation(project(":gestalt-toml-jackson3"))
                implementation(project(":gestalt-yaml-jackson3"))
            }
        }
    }
}


tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.github.gestalt.config.integration")
    }
}

