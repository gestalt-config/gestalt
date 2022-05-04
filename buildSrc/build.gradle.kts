/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    kotlin("jvm") version "1.6.21"
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()

    mavenCentral()
}

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    implementation("com.palantir.gradle.gitversion:gradle-git-version:0.15.0")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:2.0.2")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.6.21")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.20.0")
}


