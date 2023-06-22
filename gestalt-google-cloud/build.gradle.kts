plugins {
  id("gestalt.java-library-conventions")
  id("gestalt.java-test-conventions")
  id("gestalt.java-code-quality-conventions")
  id("gestalt.java-publish-conventions")
}

dependencies {
  implementation(project(":gestalt-core"))
  implementation(libs.google.storage)
  implementation(libs.google.secret)
}

tasks.jar {
  manifest {
    attributes("Automatic-Module-Name" to "org.github.gestalt.google")
  }
}
