package org.github.gestalt.config.integration

/**
 * @author Colin Redmond (c) 2023.
 */

fun main() {

    val kotlinTests = GestaltKotlinTest()
    kotlinTests.integrationTest()
    kotlinTests.integrationTestEnvVars()
    kotlinTests.integrationTestWithTypeOf()
}

