plugins {
    id("gestalt.java-common-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
  `jvm-test-suite`
}

repositories {
    mavenLocal()
    mavenCentral()
}
dependencies {
  testImplementation(project(mapOf("path" to ":gestalt-google-cloud")))
}


testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()
      targets {
        all {
          testTask.configure {
            options {
              val junitOptions = this as JUnitPlatformOptions;
              junitOptions.excludeTags = setOf("cloud")
            }
          }
        }
      }

      testType.set(TestSuiteType.UNIT_TEST)
      dependencies {
        implementation(project(":gestalt-core"))
        implementation(project(":gestalt-hocon"))
        implementation(project(":gestalt-kotlin"))
        implementation(project(":gestalt-json"))
        implementation(project(":gestalt-toml"))
        implementation(project(":gestalt-yaml"))
        implementation(project(":gestalt-s3"))

        implementation(project(":gestalt-google-cloud"))


        implementation(project(":gestalt-kodein-di"))
        implementation(libs.kodein.di)

        implementation(project(":gestalt-koin-di"))
        implementation(libs.koin.di)

        implementation(libs.aws.mock)

        implementation(project(":gestalt-guice"))
        implementation(libs.guice)
      }
    }
  }
}


tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.github.gestalt.config.integration")
    }
}

