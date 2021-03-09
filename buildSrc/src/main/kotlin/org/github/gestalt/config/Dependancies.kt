package org.github.gestalt.config

import org.github.gestalt.config.Test.Versions

/**
 * define all dependencies for the project here to keep consistent.
 */

object Application {
    object Versions {
        const val slf4j = "1.7.30"

        const val kotlinVersion = "1.4.30"

        const val jackson = "2.12.1"

        const val hocon = "1.4.1"
        const val aws = "2.15.82"
        const val jgit = "5.10.0.202012080955-r"
        const val apacheSshd = "2.6.0"
        const val eddsa = "0.3.0"
    }

    object Logging {
        const val slf4japi = "org.slf4j:slf4j-api:${Versions.slf4j}"
    }

    object Kotlin {
        const val kotlinReflection = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}"
    }

    object Json {
        const val jacksonCore = "com.fasterxml.jackson.core:jackson-core:${Versions.jackson}"
        const val jacksonDataBind = "com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}"
        const val jacksonDataformatYaml = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.jackson}"

        const val jacksonJava8 = "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${Versions.jackson}"
        const val jacksonJsr310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}"
    }

    object Hocon {
        const val hocon = "com.typesafe:config:${Versions.hocon}"
    }

    object AWS {
        const val awsS3 = "software.amazon.awssdk:s3:${Versions.aws}"
    }

    object Git {
        const val jgit = "org.eclipse.jgit:org.eclipse.jgit:${Versions.jgit}"
        const val jgitApacheSSH = "org.eclipse.jgit:org.eclipse.jgit.ssh.apache:${Versions.jgit}"
        const val eddsa = "net.i2p.crypto:eddsa:${Versions.eddsa}"
    }
}


object Test {
    private object Versions {
        const val junit5 = "5.7.1"
        const val assertJ = "3.19.0"
        const val mockito = "3.7.7"
        const val mockk = "1.10.5"
        const val kotlinTestAssertions = "4.4.0"
        const val awsMock = "2.1.28"
    }

    const val junitAPI = "org.junit.jupiter:junit-jupiter-api:${Versions.junit5}"
    const val junitEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}"
    const val assertJ = "org.assertj:assertj-core:${Versions.assertJ}"

    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"

    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val kotlinTestAssertions = "io.kotest:kotest-assertions-core-jvm:${Versions.kotlinTestAssertions}"
    const val awsMock = "com.adobe.testing:s3mock-junit5:${Versions.awsMock}"

}

object Plugins {
    object Versions {
        const val errorprone = "2.4.0"
        const val detekt = "1.15.0"
    }

    const val errorProne = "com.google.errorprone:error_prone_core:${Versions.errorprone}"
    const val detekt = "io.gitlab.arturbosch.detekt:detekt-formatting:${org.github.gestalt.config.Plugins.Versions.detekt}"
}
