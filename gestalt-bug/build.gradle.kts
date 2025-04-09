plugins {
    id("gestalt.java-common-conventions")
    `jvm-test-suite`
    jacoco
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.gestalt.aws)
    implementation(libs.gestalt.azure)
    implementation(libs.gestalt.core)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.27.3")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnit()
        }
    }
}
