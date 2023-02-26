import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * Apply the plugin to setup kotlin code plugins.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    //Kotlin
    implementation(kotlin("stdlib-jdk8"))

    // Will apply the plugin to all dokka tasks
    dokkaHtmlPlugin(libs.kotlin.dokka)
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
}


// Make sure we compile Kotlin before the Java Docs
tasks.withType<Javadoc>().configureEach {
    enabled = false
}

// needed for the java module system to work.  https://github.com/gradle/gradle/issues/17271
val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
compileKotlin.destinationDirectory.set(compileJava.destinationDirectory)
