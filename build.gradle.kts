plugins {
    base

    id("gestalt.dependency-update-conventions")
    id("gestalt.git-version-conventions")
    idea
}

allprojects {
    group = "com.github.gestalt-config"
    version = "0.30.0"
}


