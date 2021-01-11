plugins {
    id("gestalt.java-common-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
}

repositories {
    maven(url = "https://dl.bintray.com/credmond/gestalt" )
}

dependencies {
    testImplementation("org.config.gestalt:gestalt-core:0.2.0")
    testImplementation("org.config.gestalt:gestalt-kotlin:0.2.0")
}
