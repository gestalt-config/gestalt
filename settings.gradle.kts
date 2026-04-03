rootProject.name = "gestalt"
include(
    "gestalt-aws", "gestalt-azure", "gestalt-cdi",
    "gestalt-core", "gestalt-dotenv", "gestalt-hocon",
    "gestalt-json", "gestalt-json-jackson3",
    "gestalt-git", "gestalt-google-cloud", "gestalt-guice",
    "gestalt-kotlin", "gestalt-micrometer", "gestalt-kodein-di",
    "gestalt-koin-di", "gestalt-toml", "gestalt-toml-jackson3", "gestalt-validator-hibernate",
    "gestalt-vault", "gestalt-yaml", "gestalt-yaml-jackson3"
)

// testing utility projects
include(
    "code-coverage-report", "gestalt-benchmark", "gestalt-examples:gestalt-sample",
    "gestalt-examples:gestalt-sample-jackson3",
    "gestalt-examples:gestalt-sample-module", "gestalt-examples:gestalt-sample-java-records", "gestalt-test"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
