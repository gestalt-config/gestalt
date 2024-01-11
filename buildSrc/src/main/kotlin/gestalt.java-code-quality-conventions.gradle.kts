import net.ltgt.gradle.errorprone.errorprone

/**
 * Apply to all modules to include multiple code quality plugins to your module.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
plugins {
    id("net.ltgt.errorprone")
    checkstyle
    pmd
}

dependencies {
    errorprone(libs.errorProne)
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone.disableWarningsInGeneratedCode.set(true)
}

checkstyle {
    toolVersion = libs.versions.checkStyle.get()
    configFile = file(rootDir.path + "/config/checkstyle/google_checks.xml")
    isIgnoreFailures = true
}

pmd {
    isConsoleOutput = true
    ruleSets = listOf(rootDir.path + "/config/pmd/custom_ruleset.xml")
}
