/*
 * Apply the plugin to create a java-library module.
 */

plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("gestalt.java-common-conventions")

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

java {
    withSourcesJar()
}


dependencies {

}

