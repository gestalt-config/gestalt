plugins {
    id("gestalt.java-common-conventions")
    id("gestalt.java-test-conventions")
}

repositories {
    maven(url = "https://dl.bintray.com/credmond/gestalt" )
}

dependencies {
    testImplementation("org.config.gestalt:gestalt-core:0.1.0")
}
