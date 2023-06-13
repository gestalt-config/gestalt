plugins {
  id("gestalt.java-library-conventions")
  id("gestalt.java-test-conventions")
  id("gestalt.java-code-quality-conventions")
  id("gestalt.java-publish-conventions")
}

dependencies {
  implementation(project(":gestalt-core"))
  implementation(libs.aws.s3)
  implementation(libs.aws.secret)
  implementation(libs.aws.url.client)
  implementation(libs.jackson.databind)
  testImplementation(libs.aws.mock)
}


