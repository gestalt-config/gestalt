/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    kotlin("jvm") version "1.4.30"
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()

    jcenter()
    mavenCentral()
    maven(url="https://dl.bintray.com/kotlin/dokka")
}

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.36.0")
    implementation("com.palantir.gradle.gitversion:gradle-git-version:0.12.3")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:1.3.0")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.20")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.15.0")
}


