package org.github.gestalt.config.integration

/**
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */

fun main() {

    val kotlinTests = GestaltKotlinTest()
    kotlinTests.integrationTest()
    kotlinTests.integrationTestEnvVars()
    kotlinTests.integrationTestWithTypeOf()
}

