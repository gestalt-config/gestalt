import org.gradle.kotlin.dsl.the

/**
 * Part of the workaround for accessing the version catalog from the buildSrc project.
 *
 * @see https://github.com/gradle/gradle/issues/15383
 */
val org.gradle.api.Project.libs get() = the<org.gradle.accessors.dm.LibrariesForLibs>()
