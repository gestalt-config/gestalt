plugins {
    id("gestalt.java-common-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaLatest.get()))
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(project(":gestalt-core"))
                implementation(project(":gestalt-hocon"))
                implementation(project(":gestalt-kotlin"))
                implementation(project(":gestalt-json"))
                implementation(project(":gestalt-toml"))
                implementation(project(":gestalt-yaml"))
            }
        }
    }
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.github.gestalt.config.integration.latest")
    }
}
