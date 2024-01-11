/*
 * Apply the java Plugin to add support for Java.
 * Adds a set of common dependencies all modules need.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
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
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}
