/*
 * Apply the plugin to setup kotlin code quality plugins.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */

plugins {
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    detektPlugins(libs.detekt)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom("$rootDir/config/detekt/config.yml")
    debug = false
    parallel = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    exclude(".*/resources/.*")
    exclude(".*/tmp/.*")
    exclude(".*/build/.*")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

configurations.detekt {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.9.10") // Add the appropriate Kotlin version here
        }
    }
}
