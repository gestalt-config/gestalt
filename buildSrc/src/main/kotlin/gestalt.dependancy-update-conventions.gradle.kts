import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

/**
 * Apply to your main gradle build script and run with:
 * .\gradlew dependencyUpdates -Drevision=release -DoutputFormatter=json,xml
 *
 *  To automatically apply updates
 *  ./gradlew versionCatalogUpdate
 *
 * To interactively view updates
 * ./gradlew versionCatalogUpdate --interactive
 *
 * To apply the changes to the
 * ./gradlew versionCatalogApplyUpdates
 * to get a list of dependencies that may need to be updated.
 *
 *  To check for dependency updates on the BuildSrc project.
 *  You need to run manually https://github.com/ben-manes/gradle-versions-plugin/issues/284
 *  ./gradlew -p buildSrc dependencyUpdates
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
plugins {
    id("com.github.ben-manes.versions")
    id("nl.littlerobots.version-catalog-update")
}


tasks.withType<DependencyUpdatesTask> {
    // disallow release candidates as upgradable versions from stable versions
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }

    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

versionCatalogUpdate {
  // sort the catalog by key (default is true)
  sortByKey.set(false)
  // Referenced that are pinned are not automatically updated.
  // They are also not automatically kept however (use keep for that).

  keep {
    // keep versions without any library or plugin reference
    keepUnusedVersions.set(true)
    // keep all libraries that aren't used in the project
    keepUnusedLibraries.set(true)
    // keep all plugins that aren't used in the project
    keepUnusedPlugins.set(true)
  }
}
