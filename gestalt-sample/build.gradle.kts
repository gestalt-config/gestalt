plugins {
    id("gestalt.java-common-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("com.github.gestalt-config:gestalt-core:0.4.1")
    testImplementation("com.github.gestalt-config:gestalt-kotlin:0.4.1")
}
