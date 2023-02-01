plugins {
    id("gestalt.java-common-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.gestalt.core)
    implementation(libs.gestalt.hocon)
    implementation(libs.gestalt.kotlin)
    implementation(libs.gestalt.json)
    implementation(libs.gestalt.toml)
    implementation(libs.gestalt.yaml)

    implementation(libs.gestalt.guice)
    implementation(libs.guice)

    implementation(libs.junitAPI)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaLatest.get()))
    }
}
