package org.github.gestalt.config.kotlin.koin.test

import org.github.gestalt.config.Gestalt
import org.github.gestalt.config.builder.GestaltBuilder
import org.github.gestalt.config.kotlin.koin.gestalt
import org.github.gestalt.config.source.ClassPathConfigSourceBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KoinTest {
    private var gestalt: Gestalt? = null

    @BeforeAll
    fun setup() {
        val builder = GestaltBuilder()
        gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())
            .build()
        gestalt!!.loadConfigs()
    }

    @Test
    fun `test Koin DI`() {

        val koinModule = module {
            single { gestalt!! }
            single { DBService1(gestalt("db")) }
        }

        val myApp = koinApplication {
            modules(koinModule)
        }

        val dbService1: DBService1 = myApp.koin.get()
        Assertions.assertEquals("password", dbService1.getPassword())
        Assertions.assertEquals(100, dbService1.getPort())
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/mydb1", dbService1.getUri())
    }

    @Test
    fun `test Koin DI Doesnt Exist`() {

        val koinModule = module {
            single { gestalt!! }
            single { DBService1(gestalt("db")) }
        }

        val myApp = koinApplication {
            modules(koinModule)
        }

        val dbService1: DBService1 = myApp.koin.get()
        Assertions.assertEquals("password", dbService1.getPassword())
        Assertions.assertEquals(100, dbService1.getPort())
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/mydb1", dbService1.getUri())
    }

    @Test
    fun `test Koin DI Default`() {

        val koinModule = module {
            single { gestalt!! }
            single { DBService2(gestalt("notdb", DBInfoPOJO(port = 1000, password = "default"))) }
        }

        val myApp = koinApplication {
            modules(koinModule)
        }

        val dbService1: DBService2 = myApp.koin.get()
        Assertions.assertEquals("default", dbService1.getPassword())
        Assertions.assertEquals(1000, dbService1.getPort())
        Assertions.assertEquals("mysql:URI", dbService1.getUri())
    }
}
