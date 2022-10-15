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
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.compileJava {
    options.compilerArgs.addAll(arrayOf("--release", "11"))
}

dependencies {
    implementation(libs.slf4j.api)
}
