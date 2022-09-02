plugins {
    base

    id("gestalt.dependancy-update-conventions")
    id("gestalt.git-version-conventions")
    idea
}

allprojects {
    group = "com.github.gestalt-config"
    version = "0.15.0-rc-2"
}
