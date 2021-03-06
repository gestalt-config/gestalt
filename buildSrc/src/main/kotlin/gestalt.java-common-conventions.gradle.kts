import org.github.gestalt.config.Application

/*
 * Apply the java Plugin to add support for Java.
 * Adds a set of common dependancies all modules need.
 */

plugins {
    // Apply the java Plugin to add support for Java.
    java
}

repositories {
    // Use JCenter for resolving dependencies.
    jcenter()
    mavenCentral()
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(arrayOf("--release", "8"))
}

dependencies {
    implementation(org.github.gestalt.config.Application.Logging.slf4japi)
}
