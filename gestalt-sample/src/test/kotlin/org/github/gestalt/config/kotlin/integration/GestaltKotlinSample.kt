package org.github.gestalt.config.kotlin.integration

import org.github.gestalt.config.Gestalt
import org.github.gestalt.config.builder.GestaltBuilder
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.kotlin.getConfig
import org.github.gestalt.config.kotlin.kodein.gestalt
import org.github.gestalt.config.kotlin.koin.gestalt
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.source.ClassPathConfigSource
import org.github.gestalt.config.source.EnvironmentConfigSource
import org.github.gestalt.config.source.FileConfigSource
import org.github.gestalt.config.source.MapConfigSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.kodein.di.*
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.reflect.typeOf

class GestaltKotlinSample {
    @Test
    @kotlin.Throws(GestaltException::class)
    fun integrationTest() {
        val configs: MutableMap<String, String> = mutableMapOf(
            "db.hosts[0].password" to "1234",
            "db.hosts[1].password" to "5678",
            "db.hosts[2].password" to "9012",
            "db.idleTimeout" to "123"
        )

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(ClassPathConfigSource("/default.properties"))
            .addSource(ClassPathConfigSource("/dev.properties"))
            .addSource(MapConfigSource(configs))
            .build()
        gestalt.loadConfigs()
        testValidation(gestalt)
    }

    @Test
    @kotlin.Throws(GestaltException::class)
    fun integrationTestEnvVars() {
        val configs: MutableMap<String, String> = HashMap<String, String>()
        configs["db.hosts[0].password"] = "1234"
        configs["db.hosts[1].password"] = "5678"
        configs["db.hosts[2].password"] = "9012"
        val devFileURL: java.net.URL = GestaltKotlinSample::class.java.classLoader.getResource("dev.properties")
        val devFile: java.io.File = java.io.File(devFileURL.file)
        val builder = GestaltBuilder()
        val gestalt: Gestalt = builder
            .addSource(ClassPathConfigSource("/default.properties"))
            .addSource(FileConfigSource(devFile))
            .addSource(MapConfigSource(configs))
            .addSource(EnvironmentConfigSource())
            .build()
        gestalt.loadConfigs()
        testValidation(gestalt)

        assertEquals('a', gestalt.getConfig("serviceMode"))
        val booking: SubService = gestalt.getConfig("subservice.booking")
        assertTrue(booking.isEnabled)
        assertEquals("https://dev.booking.host.name", booking.service!!.host)
        assertEquals(443, booking.service!!.port)
        assertEquals("booking", booking.service!!.path)
    }

    @Test
    @ExperimentalStdlibApi
    fun integrationTestWithTypeOf() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.hosts[0].password"] = "1234"
        configs["db.hosts[1].password"] = "5678"
        configs["db.hosts[2].password"] = "9012"
        configs["db.idleTimeout"] = "123"
        val devFileURL: java.net.URL = GestaltKotlinSample::class.java.classLoader.getResource("dev.properties")
        val devFile: java.io.File = java.io.File(devFileURL.file)
        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(ClassPathConfigSource("/default.properties"))
            .addSource(FileConfigSource(devFile))
            .addSource(MapConfigSource(configs))
            .build()
        gestalt.loadConfigs()
        val pool: HttpPool = gestalt.getConfig("http.pool", KTypeCapture.of<HttpPool>(typeOf<HttpPool>())) as HttpPool
        assertEquals(1000, pool.maxTotal.toInt())
        assertEquals(1000, gestalt.getConfig("http.pool.noVal", 1000, KTypeCapture.of<Int>(typeOf<Int>())))
        assertEquals(50L, pool.maxPerRoute)
        assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute"))
        assertEquals(6000, pool.validateAfterInactivity)
        assertEquals(60000.0, pool.keepAliveTimeoutMs)
        assertEquals(25, pool.idleTimeoutSec)
        assertEquals(33.0f, pool.defaultWait)
    }

    @Test
    @kotlin.Throws(GestaltException::class)
    fun integrationTestKodein() {
        val configs: MutableMap<String, String> = mutableMapOf(
            "db.hosts[0].password" to "1234",
            "db.hosts[1].password" to "5678",
            "db.hosts[2].password" to "9012",
            "db.idleTimeout" to "123"
        )

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(ClassPathConfigSource("/default.properties"))
            .addSource(ClassPathConfigSource("/dev.properties"))
            .addSource(MapConfigSource(configs))
            .build()
        gestalt.loadConfigs()

        val kodein = DI {
            bindInstance { gestalt!! }
            bindSingleton { ConnectionService(gestalt("http.pool")) }
            bindSingleton { HostService(gestalt("DB.nohost", Host("test1", "test2", "test3"))) }
        }

        val connectionService = kodein.direct.instance<ConnectionService>()

        val pool: HttpPool = connectionService.httpPool
        assertEquals(1000, pool.maxTotal.toInt())
        assertEquals(1000.toShort(), gestalt.getConfig("http.pool.maxTotal"))
        assertEquals(50L, pool.maxPerRoute)
        assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute"))
        assertEquals(6000, pool.validateAfterInactivity)
        assertEquals(60000.0, pool.keepAliveTimeoutMs)
        assertEquals(25, pool.idleTimeoutSec)
        assertEquals(33.0f, pool.defaultWait)

        val hostService = kodein.direct.instance<HostService>()
        assertEquals("test3", hostService.hostPool.password)
        assertEquals("test2", hostService.hostPool.url)
        assertEquals("test1", hostService.hostPool.user)
    }

    @Test
    @kotlin.Throws(GestaltException::class)
    fun integrationTestKoin() {
        val configs: MutableMap<String, String> = mutableMapOf(
            "db.hosts[0].password" to "1234",
            "db.hosts[1].password" to "5678",
            "db.hosts[2].password" to "9012",
            "db.idleTimeout" to "123"
        )

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(ClassPathConfigSource("/default.properties"))
            .addSource(ClassPathConfigSource("/dev.properties"))
            .addSource(MapConfigSource(configs))
            .build()
        gestalt.loadConfigs()

        val koinModule = module {
            single { gestalt!! }
            single { ConnectionService(gestalt("http.pool")) }
            single { HostService(gestalt("DB.nohost", Host("test1", "test2", "test3"))) }
        }

        val myApp = koinApplication {
            modules(koinModule)
        }

        val connectionService: ConnectionService = myApp.koin.get()

        val pool: HttpPool = connectionService.httpPool
        assertEquals(1000, pool.maxTotal.toInt())
        assertEquals(1000.toShort(), gestalt.getConfig("http.pool.maxTotal"))
        assertEquals(50L, pool.maxPerRoute)
        assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute"))
        assertEquals(6000, pool.validateAfterInactivity)
        assertEquals(60000.0, pool.keepAliveTimeoutMs)
        assertEquals(25, pool.idleTimeoutSec)
        assertEquals(33.0f, pool.defaultWait)

        val hostService:HostService = myApp.koin.get()
        assertEquals("test3", hostService.hostPool.password)
        assertEquals("test2", hostService.hostPool.url)
        assertEquals("test1", hostService.hostPool.user)
    }

    private fun testValidation(gestalt: Gestalt) {
        val pool: HttpPool = gestalt.getConfig("http.pool")
        assertEquals(1000, pool.maxTotal.toInt())
        assertEquals(1000.toShort(), gestalt.getConfig("http.pool.maxTotal"))
        assertEquals(50L, pool.maxPerRoute)
        assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute"))
        assertEquals(6000, pool.validateAfterInactivity)
        assertEquals(60000.0, pool.keepAliveTimeoutMs)
        assertEquals(25, pool.idleTimeoutSec)
        assertEquals(33.0f, pool.defaultWait)
        var startTime: Long = System.nanoTime()
        gestalt.getConfig("db", DataBase::class.java)
        val timeTaken: Long = System.nanoTime() - startTime
        startTime = System.nanoTime()
        val db: DataBase = gestalt.getConfig("db", DataBase::class.java)
        val cacheTimeTaken: Long = System.nanoTime() - startTime

        // not really a great test for ensuring we are hitting a cache
        assertTrue(timeTaken > cacheTimeTaken)
        assertEquals(600, db.connectionTimeout)
        assertEquals(600, gestalt.getConfig("db.connectionTimeout", Int::class.java))
        assertEquals(123, db.idleTimeout)
        assertEquals(60000.0f, db.maxLifetime)
        assertNull(db.isEnabled)
        assertTrue(gestalt.getConfig("db.isEnabled", true))
        assertEquals(3, db.hosts!!.size)
        assertEquals("credmond", db.hosts!![0].user)
        assertEquals("credmond", gestalt.getConfig("db.hosts[0].user", "test"))
        assertEquals("1234", db.hosts!![0].password)
        assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", db.hosts!![0].url)
        assertEquals("credmond", db.hosts!![1].user)
        assertEquals("5678", db.hosts!![1].password)
        assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", db.hosts!![1].url)
        assertEquals("credmond", db.hosts!![2].user)
        assertEquals("9012", db.hosts!![2].password)
        assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", db.hosts!![2].url)
        assertEquals("test", gestalt.getConfig("db.does.not.exist", "test"))
        val hosts: List<Host> = gestalt.getConfig("db.hosts", emptyList())
        assertEquals(3, hosts.size)
        assertEquals("credmond", hosts[0].user)
        assertEquals("1234", hosts[0].password)
        assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", hosts[0].url)
        assertEquals("credmond", hosts[1].user)
        assertEquals("5678", hosts[1].password)
        assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", hosts[1].url)
        assertEquals("credmond", hosts[2].user)
        assertEquals("9012", hosts[2].password)
        assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", hosts[2].url)
        val noHosts: List<Host> = gestalt.getConfig("db.not.hosts", emptyList())
        assertEquals(0, noHosts.size)
        val admin: User = gestalt.getConfig("admin")
        assertEquals(3, admin.user?.size)
        assertEquals("Peter", admin.user!![0])
        assertEquals("Kim", admin.user!![1])
        assertEquals("Steve", admin.user!![2])
        assertEquals(Role.LEVEL0, admin.accessRole)
        assertTrue(admin.overrideEnabled)
        val user: User = gestalt.getConfig("employee")
        assertEquals(1, user.user?.size)
        assertEquals("Janice", user.user!![0])
        assertEquals(Role.LEVEL1, user.accessRole)
        assertFalse(user.overrideEnabled)
        assertEquals(
            "active", gestalt.getConfig(
                "serviceMode", TypeCapture.of(
                    String::class.java
                )
            )
        )
        assertEquals(
            'a', gestalt.getConfig(
                "serviceMode", TypeCapture.of(
                    Char::class.java
                )
            )
        )
    }

    enum class Role {
        LEVEL0, LEVEL1
    }

    class ConnectionService(var httpPool: HttpPool)

    class HostService(var hostPool: Host)

    data class HttpPool(
        var maxTotal: Short = 0,
        var maxPerRoute: Long = 0,
        var validateAfterInactivity: Int = 0,
        var keepAliveTimeoutMs: Double = 6000.0,
        var idleTimeoutSec: Short = 10,
        var defaultWait: Float = 33.0f
    )

    class Host(var user: String? = null, var url: String? = null, var password: String? = null)

    class DataBase {
        var hosts: List<Host>? = null
        var connectionTimeout = 0
        var idleTimeout: Int? = null
        var maxLifetime = 0f
        var isEnabled: Boolean? = null
    }

    class User {
        var user: Array<String>? = null
        var overrideEnabled: Boolean = false
        var accessRole: Role? = null
    }

    data class SubService(
        var isEnabled: Boolean = false,
        var service: Connection? = null
    )

    data class Connection(
        var host: String,
        var port: Int = 0,
        var path: String? = null
    )
}
