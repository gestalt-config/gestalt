package org.github.gestalt.config.kotlin.integration

import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.integration.GestaltConfigTest

/**
 * @author Colin Redmond (c) 2023.
 */
object MainClass {
    @Throws(GestaltException::class)
    @JvmStatic
    fun main(args: Array<String>) {

        val kotlinTests = GestaltKotlinTest()
        kotlinTests.integrationTest()
        kotlinTests.integrationTestEnvVars()
        kotlinTests.integrationTestWithTypeOf()
    }
}
