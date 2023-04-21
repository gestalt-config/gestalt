// to run ./gradlew testCodeCoverageReport
// XML and HTML reports can now be found under code-coverage-report/build/reports/jacoco/testCodeCoverageReport.
// @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
plugins {
    base
    id("jacoco-report-aggregation")
}

repositories {
    mavenCentral()
}

dependencies {
    jacocoAggregation(project(":gestalt-cdi"))
    jacocoAggregation(project(":gestalt-core"))
    jacocoAggregation(project(":gestalt-git"))
    jacocoAggregation(project(":gestalt-guice"))
    jacocoAggregation(project(":gestalt-hocon"))
    jacocoAggregation(project(":gestalt-json"))
    jacocoAggregation(project(":gestalt-kodein-di"))
    jacocoAggregation(project(":gestalt-koin-di"))
    jacocoAggregation(project(":gestalt-kotlin"))
    jacocoAggregation(project(":gestalt-s3"))
    jacocoAggregation(project(":gestalt-toml"))
    jacocoAggregation(project(":gestalt-yaml"))

    // include additional tests.
    jacocoAggregation(project(":gestalt-test"))
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }

        val integrationTestCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.INTEGRATION_TEST)
        }
    }
}

tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
    dependsOn(tasks.named<JacocoReport>("integrationTestCodeCoverageReport"))
}
