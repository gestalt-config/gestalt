import org.github.gestalt.config.Test

/*
 * Apply the plugin to setup testing dependencies and tasks.
 */

plugins {
    id("gestalt.java-common-conventions")
    jacoco
}

dependencies {
    // Use JUnit Jupiter API for testing.
    testImplementation(org.github.gestalt.config.Test.junitAPI)

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly(org.github.gestalt.config.Test.junitEngine)

    testImplementation(org.github.gestalt.config.Test.assertJ)

    testImplementation(org.github.gestalt.config.Test.mockito)
}

tasks.test {
    // Use junit platform for unit tests
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.compileTestJava {
    sourceCompatibility = JavaVersion.VERSION_16.toString()
    targetCompatibility = JavaVersion.VERSION_16.toString()
}

//setup Jacoco
apply(plugin = "jacoco")
tasks.withType<JacocoReport> {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}
