rootProject.name = "gestalt"
include(
    "gestalt-core", "gestalt-hocon", "gestalt-json", "gestalt-git", "gestalt-kotlin", "gestalt-kodein-di",
    "gestalt-koin-di", "gestalt-s3", "gestalt-sample", "gestalt-sample-java-latest", "gestalt-yaml"
)

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
