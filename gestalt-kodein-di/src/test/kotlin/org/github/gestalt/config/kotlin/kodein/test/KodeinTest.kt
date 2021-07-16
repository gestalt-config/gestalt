package org.github.gestalt.config.kotlin.kodein.test

import org.github.gestalt.config.Gestalt
import org.github.gestalt.config.builder.GestaltBuilder
import org.github.gestalt.config.kotlin.kodein.gestalt
import org.github.gestalt.config.kotlin.kodein.gestaltDefault
import org.github.gestalt.config.source.ClassPathConfigSource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kodein.di.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KodeinTest {

    var gestalt: Gestalt? = null

    @BeforeAll
    fun setup() {
        val builder = GestaltBuilder()
        gestalt = builder
            .addSource(ClassPathConfigSource("/default.properties"))
            .build()
        gestalt!!.loadConfigs()
    }

    @Test
    fun testKodeinDI() {

        val kodein = DI {
            bindInstance { gestalt!! }
            bindSingleton { DBService1(gestalt("db")) }
        }

        val dbService1 = kodein.direct.instance<DBService1>()
        Assertions.assertEquals("password", dbService1.getPassword())
        Assertions.assertEquals(100, dbService1.getPort())
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/mydb1", dbService1.getUri())
    }

    @Test
    fun testKodeinDIDoesntExist() {

        val kodein = DI {
            bindInstance { gestalt!! }
            bindSingleton { DBService1(gestalt("db")) }
        }

        val dbService1 = kodein.direct.instance<DBService1>()
        Assertions.assertEquals("password", dbService1.getPassword())
        Assertions.assertEquals(100, dbService1.getPort())
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/mydb1", dbService1.getUri())
    }

    @Test
    fun testKodeinDIDefault() {

        val kodein = DI {
            bindInstance { gestalt!! }
            bindSingleton(tag = "default") { DBService2(gestaltDefault("notdb", DBInfoPOJO(port = 1000, password = "default"))) }
            bindSingleton(tag = "actual") { DBService2(gestaltDefault("db", DBInfoPOJO(port = 2000, password = "test"))) }
        }

        val dbService1 = kodein.direct.instance<DBService2>(tag = "default")
        Assertions.assertEquals("default", dbService1.getPassword())
        Assertions.assertEquals(1000, dbService1.getPort())
        Assertions.assertEquals("mysql:URI", dbService1.getUri())
    }

    @Test
    fun testKodeinDIDefaultExists() {

        val kodein = DI {
            bindInstance { gestalt!! }
            bindSingleton(tag = "default") { DBService2(gestaltDefault("notdb", DBInfoPOJO(port = 1000, password = "default"))) }
            bindSingleton(tag = "actual") { DBService2(gestaltDefault("db", DBInfoPOJO(port = 2000, password = "test"))) }
        }

        val dbService2 = kodein.direct.instance<DBService2>(tag = "actual")
        Assertions.assertEquals("password", dbService2.getPassword())
        Assertions.assertEquals(100, dbService2.getPort())
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/mydb1", dbService2.getUri())
    }

    @Test
    fun testKodeinDIBy() {

        val kodein = DI {
            bindInstance { gestalt!! }
            bindSingleton { DBService1(gestalt("db")) }
        }

        val dbService1: DBService1 by kodein.instance()
        Assertions.assertEquals("password", dbService1.getPassword())
        Assertions.assertEquals(100, dbService1.getPort())
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/mydb1", dbService1.getUri())
    }

    @Test
    fun testKodeinDIWithTag() {

        val kodein = DI {
            bindInstance(tag = "gestalt") { gestalt!! }
            bindSingleton { DBService2(gestaltDefault("db", DBInfoPOJO(port = 1000, password = "default"), "gestalt")) }
        }

        val dbService2 = kodein.direct.instance<DBService2>()
        Assertions.assertEquals("password", dbService2.getPassword())
        Assertions.assertEquals(100, dbService2.getPort())
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/mydb1", dbService2.getUri())
    }

    @Test
    fun testKodeinDIWithWrongTag() {

        val kodein = DI {
            bindInstance(tag = "gestalt") { gestalt!! }
            bindSingleton { DBService2(gestaltDefault("notdb", DBInfoPOJO(port = 1000, password = "default"), "notATag")) }
        }

        Assertions.assertThrows(DI.NotFoundException::class.java) { kodein.direct.instance<DBService2>() }

    }
}
