plugins {
    id("gestalt.java-common-conventions")
    id("gestalt.kotlin-common-conventions")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.gestalt.aws)
    implementation(libs.gestalt.core)
    implementation(libs.gestalt.git)
    implementation(libs.gestalt.google)
    implementation(libs.gestalt.guice)
    implementation(libs.gestalt.hocon)
    implementation(libs.gestalt.kotlin)
    implementation(libs.gestalt.micrometer)
    implementation(libs.gestalt.json)
    implementation(libs.gestalt.toml)
    implementation(libs.gestalt.hibernate)
    implementation(libs.gestalt.vault)
    implementation(libs.gestalt.yaml)

    implementation(libs.junitAPI)
    implementation(libs.assertJ)
    implementation(libs.aws.mock)
    implementation(libs.guice)

    implementation(libs.micrometer)

    implementation(libs.hibernate.validator)
    implementation(libs.expressly)

    testImplementation(libs.junitAPI)
    testImplementation(libs.assertJ)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaLatest.get()))
    }
}

tasks.named("compileJava", JavaCompile::class.java) {
    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        // Provide compiled Kotlin classes to javac – needed for Java/Kotlin mixed sources to work
        listOf("--patch-module", "org.github.gestalt.config.integration=${sourceSets["main"].output.asPath}")
    })
}
