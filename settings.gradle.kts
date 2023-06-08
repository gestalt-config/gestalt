rootProject.name = "gestalt"
include(
    "gestalt-cdi", "gestalt-core", "gestalt-hocon", "gestalt-json", "gestalt-git",
    "gestalt-google-cloud", "gestalt-guice", "gestalt-kotlin", "gestalt-kodein-di",
    "gestalt-koin-di", "gestalt-s3", "gestalt-toml", "gestalt-yaml"
)

// testing utility projects
include(
    "code-coverage-report", "gestalt-benchmark", "gestalt-examples:gestalt-sample",
    "gestalt-examples:gestalt-sample-module", "gestalt-examples:gestalt-sample-java-latest", "gestalt-test")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
