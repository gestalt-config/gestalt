rootProject.name = "gestalt"
include(
    "gestalt-benchmark", "gestalt-core", "gestalt-hocon", "gestalt-json", "gestalt-git", "gestalt-kotlin", "gestalt-kodein-di",
    "gestalt-koin-di", "gestalt-s3", "gestalt-sample", "gestalt-sample-java-latest", "gestalt-toml", "gestalt-yaml"
)

includeBuild("build-logic")

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
