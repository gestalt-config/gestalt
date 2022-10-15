
/**
 * Part of the workaround for accessing the version catalog from the buildSrc project.
 *
 * @see https://github.com/gradle/gradle/issues/15383
 */
dependencyResolutionManagement {
    repositories {
        // Use the plugin portal to apply community plugins in convention plugins.
        gradlePluginPortal()

        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
