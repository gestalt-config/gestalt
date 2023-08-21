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
    implementation(libs.gestalt.aws)
    implementation(libs.gestalt.core)
    implementation(libs.gestalt.git)
    implementation(libs.gestalt.google)
    implementation(libs.gestalt.guice)
    implementation(libs.gestalt.hocon)
    implementation(libs.gestalt.kotlin)
    implementation(libs.gestalt.json)
    implementation(libs.gestalt.toml)
    implementation(libs.gestalt.vault)
    implementation(libs.gestalt.yaml)

    implementation(libs.junitAPI)
    implementation(libs.aws.mock)
    implementation(libs.guice)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaLatest.get()))
    }
}
