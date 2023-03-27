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


        implementation(project(":gestalt-kodein-di"))
        implementation(libs.kodein.di)

        implementation(project(":gestalt-koin-di"))
        implementation(libs.koin.di)

        implementation(libs.aws.mock)

        implementation(project(":gestalt-guice"))
        implementation(libs.guice)
      }
    }

    val integrationTest by registering(JvmTestSuite::class) {
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

        implementation(libs.gestalt.kodein.di)
        implementation(libs.kodein.di)

        implementation(libs.gestalt.koin.di)
        implementation(libs.koin.di)

        implementation(libs.aws.mock)

        implementation(libs.gestalt.guice)
        implementation(libs.guice)
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
  dependsOn(testing.suites.named("integrationTest"))
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.github.gestalt.config.integration")
    }
}

