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

dependencies {
    testImplementation(libs.gestalt.core)
    testImplementation(libs.gestalt.hocon)
    testImplementation(libs.gestalt.kotlin)
    testImplementation(libs.gestalt.json)
    testImplementation(libs.gestalt.toml)
    testImplementation(libs.gestalt.yaml)
    testImplementation(libs.gestalt.s3)

    testImplementation(libs.gestalt.kodein.di)
    implementation(libs.kodein.di)

    testImplementation(libs.gestalt.koin.di)
    implementation(libs.koin.di)

    testImplementation(libs.aws.mock)

    testImplementation(libs.gestalt.guice)
    implementation(libs.guice)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.github.gestalt.config.integration")
    }
}
