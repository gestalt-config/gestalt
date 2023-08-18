package org.github.gestalt.config.integration

import org.github.gestalt.config.exceptions.GestaltException

/**
 * @author Colin Redmond (c) 2023.
 */
object MainClassKt {
    @Throws(GestaltException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val kotlinTests = GestaltKotlinTest()
        kotlinTests.integrationTest()
        kotlinTests.integrationTestEnvVars()
        kotlinTests.integrationTestWithTypeOf()
    }
}
