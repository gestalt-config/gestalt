import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * Apply the plugin to setup kotlin code plugins.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

java {
    //withSourcesJar() // part of the publisher plugin
    //withJavadocJar()
}

dependencies {
    // Will apply the plugin to all dokka tasks
    dokkaHtmlPlugin(libs.kotlin.dokka)
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(layout.buildDirectory.get().asFile.resolve("dokka"))
}


// Make sure we compile Kotlin before the Java Docs
tasks.withType<Javadoc>().configureEach {
    enabled = false
}
