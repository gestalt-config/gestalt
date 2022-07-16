package org.github.gestalt.config

/**
 * define all dependencies for the project here to keep consistent.
 */

object Application {
    object Versions {
        const val slf4j = "1.7.36"

        const val kotlinVersion = "1.7.10"
        const val kodeinDIVersion = "7.13.1"
        const val koinDIVersion = "3.2.0"

        const val jackson = "2.13.3"

        const val hocon = "1.4.2"
        const val aws = "2.17.233"
        const val jgit = "6.2.0.202206071550-r"

        const val eddsa = "0.3.0"

    }

    object Logging {
        const val slf4japi = "org.slf4j:slf4j-api:${Versions.slf4j}"
        const val slf4jSimple = "org.slf4j:slf4j-simple:${Versions.slf4j}"

    }

    object Kotlin {
        const val kotlinReflection = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}"
        const val kodeinDI = "org.kodein.di:kodein-di-jvm:${Versions.kodeinDIVersion}"
        const val koinDI = "io.insert-koin:koin-core:${Versions.koinDIVersion}"
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
        const val junit5 = "5.8.2"
        const val assertJ = "3.23.1"
        const val mockito = "4.6.1"
        const val mockk = "1.12.4"
        const val kotlinTestAssertions = "5.3.2"
        const val awsMock = "2.4.13"
    }

    const val junitAPI = "org.junit.jupiter:junit-jupiter-api:${Versions.junit5}"
    const val junitEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}"
    const val assertJ = "org.assertj:assertj-core:${Versions.assertJ}"

    const val mockito = "org.mockito:mockito-inline:${Versions.mockito}"

    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val kotlinTestAssertions = "io.kotest:kotest-assertions-core-jvm:${Versions.kotlinTestAssertions}"
    const val awsMock = "com.adobe.testing:s3mock-junit5:${Versions.awsMock}"

}

object Plugins {
    object Versions {
        const val errorprone = "2.14.0"
        const val errorpronejavac = "9+181-r4173-1"
        const val detekt = "1.20.0"
    }

    const val errorProne = "com.google.errorprone:error_prone_core:${Versions.errorprone}"
    const val errorProneJavac = "com.google.errorprone:javac:${Versions.errorpronejavac}"
    const val detekt = "io.gitlab.arturbosch.detekt:detekt-formatting:${org.github.gestalt.config.Plugins.Versions.detekt}"
}
