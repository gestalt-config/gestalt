rootProject.name = "gestalt"
include(
  "gestalt-aws", "gestalt-azure", "gestalt-cdi", "gestalt-core", "gestalt-hocon", "gestalt-json", "gestalt-git",
  "gestalt-google-cloud", "gestalt-guice", "gestalt-kotlin", "gestalt-micrometer", "gestalt-kodein-di",
  "gestalt-koin-di", "gestalt-toml", "gestalt-validator-hibernate", "gestalt-vault", "gestalt-yaml"
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
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
