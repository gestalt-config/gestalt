import org.github.gestalt.config.Application
import org.github.gestalt.config.Test

plugins {
    id("gestalt.java-library-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.java-code-quality-conventions")
    id("gestalt.java-publish-conventions")
}

dependencies {
    implementation(project(":gestalt-core"))
    implementation(Application.AWS.awsS3)
    testImplementation(Test.awsMock)
}


