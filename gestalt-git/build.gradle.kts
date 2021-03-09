import org.github.gestalt.config.Application

plugins {
    id("gestalt.kotlin-common-conventions")
    id("gestalt.kotlin-test-conventions")
    id("gestalt.kotlin-code-quality-conventions")
    id("gestalt.java-publish-conventions")
}

dependencies {
    implementation(project(":gestalt-core"))
    //implementation(Application.Git.jgit)
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.3.2.201906051522-r")
}

