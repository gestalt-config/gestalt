
plugins {
    base

    id("gestalt.dependancy-update-conventions")
    id("gestalt.git-version-conventions")
    idea
}

allprojects {
    group = "org.config.gestalt"
    version = "0.3.1"
}
