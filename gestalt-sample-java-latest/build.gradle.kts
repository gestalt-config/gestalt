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
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    testImplementation(libs.gestalt.core)
    testImplementation(libs.gestalt.hocon)
    testImplementation(libs.gestalt.kotlin)
    testImplementation(libs.gestalt.json)
    testImplementation(libs.gestalt.toml)
    testImplementation(libs.gestalt.yaml)
    testImplementation(libs.gestalt.s3)

    testImplementation(libs.aws.mock)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.github.gestalt.config.integration.latest")
    }
}
