import org.github.gestalt.config.Test

plugins {
    jacoco
    id("gestalt.kotlin-common-conventions")
}

dependencies {
    //Testing dependencies
    testImplementation(Test.junitAPI)
    testRuntimeOnly(Test.junitEngine)
    testImplementation(Test.mockk)
    testImplementation(Test.kotlinTestAssertions)
    testImplementation(Test.mockito)
}

tasks.test {
    // Use junit platform for unit tests
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

//setup Jacoco
apply(plugin = "jacoco")
jacoco {
    toolVersion = "0.8.7"
}

tasks.withType<JacocoReport> {
    reports {
        xml.required.set(false)
        html.required.set(false)
    }
}


