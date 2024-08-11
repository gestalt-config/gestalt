plugins {
    id("gestalt.java-library-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.java-code-quality-conventions")
    id("gestalt.java-publish-conventions")
}

dependencies {
    implementation(project(":gestalt-core"))
    implementation(libs.cdi)
    testImplementation(libs.weld.junit5) {
        exclude(group = "javax.enterprise", module = "cdi-api")
    }
    testImplementation(libs.weld.core)
    testImplementation(libs.junit.testkit)
    testImplementation(libs.junit.commons)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.github.gestalt.cdi")
    }
}

