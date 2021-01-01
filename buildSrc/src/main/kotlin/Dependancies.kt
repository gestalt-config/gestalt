/**
 * define all dependencies for the project here to keep consistent.
 */

object Application {
    object Versions {
        const val slf4j = "1.7.30"
    }

    object Logging {
        const val slf4japi = "org.slf4j:slf4j-api:${Versions.slf4j}"
    }
}

object Test {
    private object Versions {
        const val junit5 = "5.7.0"
        const val assertJ = "3.18.1"
        const val mockito = "3.6.28"
    }

    const val junitAPI = "org.junit.jupiter:junit-jupiter-api:${Versions.junit5}"
    const val junitEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}"
    const val assertJ = "org.assertj:assertj-core:${Versions.assertJ}"

    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"

}

object Plugins {
    private object Versions {
        const val errorprone = "2.4.0"
        const val errorproneJava8 = "9+181-r4173-1"
    }

    const val errorProne = "com.google.errorprone:error_prone_core:${Versions.errorprone}"
    const val errorProneJava8 = "com.google.errorprone:javac:${Versions.errorproneJava8}"
}
