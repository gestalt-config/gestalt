import org.github.gestalt.config.Test

plugins {
    jacoco
    id("gestalt.kotlin-common-conventions")
}

dependencies {
    //Testing dependencies
    testImplementation(org.github.gestalt.config.Test.junitAPI)
    testRuntimeOnly(org.github.gestalt.config.Test.junitEngine)
    testImplementation(org.github.gestalt.config.Test.mockk)
    testImplementation(org.github.gestalt.config.Test.kotlinTestAssertions)
    testImplementation(org.github.gestalt.config.Test.mockito)
}

tasks.test {
    // Use junit platform for unit tests
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

//setup Jacoco
apply(plugin = "jacoco")
tasks.withType<JacocoReport> {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}


