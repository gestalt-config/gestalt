plugins {
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
    id("gestalt.kotlin-code-quality-conventions")
    id("gestalt.java-publish-conventions")
}

dependencies {
    implementation(project(":gestalt-core"))
    implementation(project(":gestalt-kotlin"))
    implementation(org.github.gestalt.config.Application.Kotlin.kodeinDI)
}
