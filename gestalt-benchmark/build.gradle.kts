plugins {
    `java-library`
    alias(libs.plugins.jmh)
}

repositories {
    mavenLocal()
    mavenCentral()
}

var gestaltVersion: String = if (project.hasProperty("gestaltVersion")) {
    project.property("gestaltVersion") as String
} else if (System.getProperty("gestaltVersion") != null) {
    System.getProperty("gestaltVersion")
} else {
    libs.versions.gestalt.get()
}

var jdkVersion: Int = if (project.hasProperty("jdkVersion")) {
    Integer.parseInt(project.property("jdkVersion") as String)
} else if (System.getProperty("jdkVersion") != null) {
    Integer.parseInt(System.getProperty("jdkVersion"))
} else {
    Integer.parseInt(libs.versions.java.get())
}

println("Running benchmarks with Gestalt Version: $gestaltVersion and JDK $jdkVersion")

dependencies {
    jmh(libs.jmh)
    jmh(libs.jmh.annotations)

    // this is the line that solves the missing /META-INF/BenchmarkList error
    jmhAnnotationProcessor(libs.jmh.annotations)

    implementation("com.github.gestalt-config:gestalt-core:${gestaltVersion}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(jdkVersion))
    }
}

jmh {
    // setup
    failOnError.set(true)
    resultsFile.set(File("${project.projectDir}/results/results-${gestaltVersion}-jdk-${jdkVersion}.json"))
    resultFormat.set("JSON")

    // Warmup
    warmupIterations.set(2)
    warmup.set("5s")

    // Benchmarks
    fork.set(2)
    iterations.set(5)
    timeOnIteration.set("10s")


    jvmArgs.set(listOf("-Xmx1G", "-Xms1G", "-XX:+UseG1GC"))

}

// to view results https://jmh.morethan.io/
