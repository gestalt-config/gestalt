/*
 * Apply the java Plugin to add support for Java.
 * Adds a set of common dependencies all modules need.
 */

plugins {
    // Apply the java Plugin to add support for Java.
    java
}

repositories {
    mavenCentral()
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(libs.slf4j.api)
}
