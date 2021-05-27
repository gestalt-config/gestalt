package org.github.gestalt.config

import org.github.gestalt.config.Test.Versions

/**
 * define all dependencies for the project here to keep consistent.
 */

object Application {
    object Versions {
        const val slf4j = "1.7.30"

        const val kotlinVersion = "1.5.10"

        const val jackson = "2.12.3"

        const val hocon = "1.4.1"
        const val aws = "2.16.73"
        const val jgit = "5.11.1.202105131744-r"

        const val eddsa = "0.3.0"

    }

    object Logging {
        const val slf4japi = "org.slf4j:slf4j-api:${Versions.slf4j}"
        const val slf4jSimple = "org.slf4j:slf4j-simple:${Versions.slf4j}"

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
        const val junit5 = "5.7.2"
        const val assertJ = "3.19.0"
        const val mockito = "3.10.0"
        const val mockk = "1.11.0"
        const val kotlinTestAssertions = "4.6.0"
        const val awsMock = "2.1.29"
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
        const val errorprone = "2.6.0"
        const val errorpronejavac = "9+181-r4173-1"
        const val detekt = "1.17.1"
    }

    const val errorProne = "com.google.errorprone:error_prone_core:${Versions.errorprone}"
    const val errorProneJavac = "com.google.errorprone:javac:${Versions.errorpronejavac}"
    const val detekt = "io.gitlab.arturbosch.detekt:detekt-formatting:${org.github.gestalt.config.Plugins.Versions.detekt}"
}
