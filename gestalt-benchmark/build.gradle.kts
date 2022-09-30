import org.github.gestalt.config.Test
import kotlin.time.measureTime

plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.8"
}

repositories {
    mavenLocal()
    mavenCentral()
}

val gestaltVersion = "0.10.0"

dependencies {
    jmh(Test.jmh)
    jmh(Test.jmhAnnotation)

    // this is the line that solves the missing /META-INF/BenchmarkList error
    jmhAnnotationProcessor(Test.jmhAnnotation)

    implementation("com.github.gestalt-config:gestalt-core:$gestaltVersion")
    implementation("com.github.gestalt-config:gestalt-kotlin:$gestaltVersion")

}


jmh {
    // setup
    failOnError.set(true)
    resultsFile.set(File("${project.projectDir}/results/results-${gestaltVersion}.json"))
    resultFormat.set("JSON")

    // Warmup
    warmupIterations.set(2)
    warmup.set("5s")

    // Benchmarks
    fork.set(2)
    iterations.set(5)
    timeOnIteration.set("5s")
}

// to view results https://jmh.morethan.io/
