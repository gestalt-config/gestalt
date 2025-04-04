plugins {
    id("gestalt.java-library-conventions")
    id("gestalt.java-test-conventions")
    id("gestalt.java-code-quality-conventions")
    id("gestalt.java-publish-conventions")
}

val patchArgs = listOf("-parameters")
tasks.compileTestJava {
    options.compilerArgs.addAll(patchArgs)
}
