// to run ./gradlew testCodeCoverageReport
// XML and HTML reports can now be found under code-coverage-report/build/reports/jacoco/testCodeCoverageReport.
// @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
plugins {
    base
    id("jacoco-report-aggregation")
}

repositories {
    mavenCentral()
}

dependencies {
    jacocoAggregation(project(":gestalt-aws"))
    jacocoAggregation(project(":gestalt-cdi"))
    jacocoAggregation(project(":gestalt-core"))
    jacocoAggregation(project(":gestalt-git"))
    jacocoAggregation(project(":gestalt-google-cloud"))
    jacocoAggregation(project(":gestalt-guice"))
    jacocoAggregation(project(":gestalt-hocon"))
    jacocoAggregation(project(":gestalt-json"))
    jacocoAggregation(project(":gestalt-kodein-di"))
    jacocoAggregation(project(":gestalt-koin-di"))
    jacocoAggregation(project(":gestalt-kotlin"))
    jacocoAggregation(project(":gestalt-micrometer"))
    jacocoAggregation(project(":gestalt-toml"))
    jacocoAggregation(project(":gestalt-validator-hibernate"))
    jacocoAggregation(project(":gestalt-vault"))
    jacocoAggregation(project(":gestalt-yaml"))


    // include additional tests.
    jacocoAggregation(project(":gestalt-test"))
    jacocoAggregation(project(":gestalt-examples:gestalt-sample"))
    jacocoAggregation(project(":gestalt-examples:gestalt-sample-java-latest"))
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
