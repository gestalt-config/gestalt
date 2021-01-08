import org.config.gestalt.Application

plugins {
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
    id("gestalt.kotlin-code-quality-conventions")
}

dependencies {
    implementation(project(":gestalt-core"))
    implementation(Application.Kotlin.kotlinReflection)
}

