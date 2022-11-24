rootProject.name = "gestalt"
include(
    "gestalt-core", "gestalt-hocon", "gestalt-json", "gestalt-git", "gestalt-kotlin", "gestalt-kodein-di",
    "gestalt-koin-di", "gestalt-s3", "gestalt-toml", "gestalt-yaml"
)

includeBuild("build-logic")

// testing utility projects
include("code-coverage-report", "gestalt-benchmark", "gestalt-sample", "gestalt-sample-java-latest", "gestalt-test")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
