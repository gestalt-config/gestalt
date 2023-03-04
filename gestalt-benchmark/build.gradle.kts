plugins {
    `java-library`
    alias(libs.plugins.jmh)
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    jmh(libs.jmh)
    jmh(libs.jmh.annotations)

    // this is the line that solves the missing /META-INF/BenchmarkList error
    jmhAnnotationProcessor(libs.jmh.annotations)

    implementation(libs.gestalt.core)
    implementation(libs.gestalt.kotlin)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

jmh {
    // setup
    failOnError.set(true)
    resultsFile.set(File("${project.projectDir}/results/results-${libs.versions.gestalt.get()}.json"))
    resultFormat.set("JSON")

    // Warmup
    warmupIterations.set(2)
    warmup.set("5s")

    // Benchmarks
    fork.set(2)
    iterations.set(5)
    timeOnIteration.set("30s")

    jvmArgs.set(listOf("-Xmx1G", "-Xms1G", "-XX:+UseG1GC"))
}

// to view results https://jmh.morethan.io/
