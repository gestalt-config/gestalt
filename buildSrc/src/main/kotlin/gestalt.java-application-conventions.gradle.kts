/*
 * Apply to create a module that can be run as a CLI application in Java.
 */

plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("gestalt.java-common-conventions")

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}
