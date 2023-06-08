plugins {
    id("gestalt.java-library-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.java-code-quality-conventions")
    id("gestalt.java-publish-conventions")
}

dependencies {
    implementation(project(":gestalt-core"))
    implementation(platform("com.google.cloud:libraries-bom:" + libs.versions.gcp.get()))
    implementation("com.google.cloud:google-cloud-storage")
}

tasks.jar {
  manifest {
    attributes("Automatic-Module-Name" to "org.github.gestalt.google")
  }
}
