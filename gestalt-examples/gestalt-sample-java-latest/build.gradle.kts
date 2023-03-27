plugins {
    id("gestalt.java-common-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaLatest.get()))
    }
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()
      testType.set(TestSuiteType.UNIT_TEST)
      dependencies {
        implementation(project(":gestalt-core"))
        implementation(project(":gestalt-hocon"))
        implementation(project(":gestalt-kotlin"))
        implementation(project(":gestalt-json"))
        implementation(project(":gestalt-toml"))
        implementation(project(":gestalt-yaml"))
        implementation(project(":gestalt-s3"))

        implementation(libs.aws.mock)
      }
    }

    val integrationTestLatest by registering(JvmTestSuite::class) {
      useJUnitJupiter()
      testType.set(TestSuiteType.INTEGRATION_TEST)
      dependencies {
        implementation(libs.gestalt.core)
        implementation(libs.gestalt.hocon)
        implementation(libs.gestalt.kotlin)
        implementation(libs.gestalt.json)
        implementation(libs.gestalt.toml)
        implementation(libs.gestalt.yaml)
        implementation(libs.gestalt.s3)

        implementation(libs.aws.mock)

      }

      targets {
        all {
          testTask.configure {
            shouldRunAfter(test)
          }
        }
      }
    }
  }
}

tasks.named("check") {
  dependsOn(testing.suites.named("integrationTestLatest"))
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.github.gestalt.config.integration.latest")
    }
}
