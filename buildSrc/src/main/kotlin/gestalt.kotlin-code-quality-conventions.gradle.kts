plugins {
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    detektPlugins(org.github.gestalt.config.Plugins.detekt)
}

detekt {
    toolVersion = org.github.gestalt.config.Plugins.Versions.detekt
    config = files(project.rootDir.resolve("config/detekt/config.yml"))
    debug = false
    parallel = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    exclude(".*/resources/.*")
    exclude(".*/tmp/.*")
    exclude(".*/build/.*")

    reports {
        xml.required.set(false)
        html.required.set(false)
    }
}
