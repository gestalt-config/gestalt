package org.github.gestalt.config
/**
 * define all dependencies for the project here to keep consistent.
 */

object Application {
    object Versions {
        const val slf4j = "1.7.30"

        const val kotlinVersion = "1.4.21"
    }

    object Logging {
        const val slf4japi = "org.slf4j:slf4j-api:${org.github.gestalt.config.Application.Versions.slf4j}"
    }

    object Kotlin {
        const val kotlinReflection = "org.jetbrains.kotlin:kotlin-reflect:${org.github.gestalt.config.Application.Versions.kotlinVersion}"
    }
}


object Test {
    private object Versions {
        const val junit5 = "5.7.0"
        const val assertJ = "3.18.1"
        const val mockito = "3.7.0"
        const val mockk = "1.10.4"
        const val kotlinTestAssertions = "4.3.2"
    }

    const val junitAPI = "org.junit.jupiter:junit-jupiter-api:${org.github.gestalt.config.Test.Versions.junit5}"
    const val junitEngine = "org.junit.jupiter:junit-jupiter-engine:${org.github.gestalt.config.Test.Versions.junit5}"
    const val assertJ = "org.assertj:assertj-core:${org.github.gestalt.config.Test.Versions.assertJ}"

    const val mockito = "org.mockito:mockito-core:${org.github.gestalt.config.Test.Versions.mockito}"

    const val mockk = "io.mockk:mockk:${org.github.gestalt.config.Test.Versions.mockk}"
    const val kotlinTestAssertions = "io.kotest:kotest-assertions-core-jvm:${org.github.gestalt.config.Test.Versions.kotlinTestAssertions}"

}

object Plugins {
    object Versions {
        const val errorprone = "2.4.0"
        const val detekt = "1.15.0"
    }

    const val errorProne = "com.google.errorprone:error_prone_core:${org.github.gestalt.config.Plugins.Versions.errorprone}"
    const val detekt = "io.gitlab.arturbosch.detekt:detekt-formatting:${org.github.gestalt.config.Plugins.Versions.detekt}"
}
