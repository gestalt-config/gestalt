import org.github.gestalt.config.Application

plugins {
    id("gestalt.java-library-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.java-code-quality-conventions")
    id("gestalt.java-publish-conventions")
}

dependencies {
    implementation(project(":gestalt-core"))
    implementation(Application.Json.jacksonCore)
    implementation(Application.Json.jacksonDataBind)

    implementation(Application.Json.jacksonJava8)
    implementation(Application.Json.jacksonJsr310)
}

