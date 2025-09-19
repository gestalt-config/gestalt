package org.github.gestalt.config.kotlin.integration

import org.github.gestalt.config.builder.GestaltBuilder
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.kotlin.entity.DataClassHasNoConstructor
import org.github.gestalt.config.kotlin.getConfig
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.source.EnvironmentConfigSourceBuilder
import org.github.gestalt.config.source.FileConfigSourceBuilder
import org.github.gestalt.config.source.MapConfigSourceBuilder
import org.github.gestalt.config.tag.Tags
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class KotlinGestaltIntegrationTests {
    @Test
    @Throws(GestaltException::class)
    @SuppressWarnings("LongMethod")
    fun integrationTest() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.hosts[0].password"] = "1234"
        configs["db.hosts[1].password"] = "5678"
        configs["db.hosts[2].password"] = "9012"
        configs["db.idleTimeout"] = "123"
        val defaultFileURL = KotlinGestaltIntegrationTests::class.java.classLoader.getResource("default.properties")
        val defaultFile = File(defaultFileURL.file)
        val devFileURL = KotlinGestaltIntegrationTests::class.java.classLoader.getResource("dev.properties")
        val devFile = File(devFileURL.file)
        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(FileConfigSourceBuilder.builder().setFile(defaultFile).build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build()
        gestalt.loadConfigs()
        val pool = gestalt.getConfig<HttpPool>("http.pool")
        Assertions.assertEquals(1000, pool.maxTotal.toInt())
        Assertions.assertEquals(1000.toShort(), gestalt.getConfig("http.pool.maxTotal"))
        Assertions.assertEquals(50L, pool.maxPerRoute)
        Assertions.assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute"))
        Assertions.assertEquals(6000, pool.validateAfterInactivity)
        Assertions.assertEquals(60000.0, pool.keepAliveTimeoutMs)
        Assertions.assertEquals(25, pool.idleTimeoutSec)
        Assertions.assertEquals(33.0f, pool.defaultWait)

        val dataClassHasNoConstructor = DataClassHasNoConstructor("test", "test")
        Assertions.assertEquals("Data Class has no constructor: test, on path: test", dataClassHasNoConstructor.description())

        var startTime = System.nanoTime()
        gestalt.getConfig("db", DataBase::class.java)
        val timeTaken = System.nanoTime() - startTime
        startTime = System.nanoTime()
        val db = gestalt.getConfig("db", DataBase::class.java)
        val cacheTimeTaken = System.nanoTime() - startTime

        // not really a great test for ensuring we are hitting a cache
        Assertions.assertTrue(timeTaken > cacheTimeTaken)
        Assertions.assertEquals(600, db.connectionTimeout)
        Assertions.assertEquals(600, gestalt.getConfig("db.connectionTimeout"))
        Assertions.assertEquals(123, db.idleTimeout)
        Assertions.assertEquals(60000.0f, db.maxLifetime)
        Assertions.assertTrue(db.isEnabled!!)
        Assertions.assertTrue(gestalt.getConfig("db.isEnabled", true))
        Assertions.assertEquals(3, db.hosts!!.size)
        Assertions.assertEquals("credmond", db.hosts!![0].user)
        Assertions.assertEquals("credmond", gestalt.getConfig("db.hosts[0].user", "test"))
        Assertions.assertEquals("1234", db.hosts!![0].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", db.hosts!![0].url)
        Assertions.assertEquals("credmond", db.hosts!![1].user)
        Assertions.assertEquals("5678", db.hosts!![1].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", db.hosts!![1].url)
        Assertions.assertEquals("credmond", db.hosts!![2].user)
        Assertions.assertEquals("9012", db.hosts!![2].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", db.hosts!![2].url)
        Assertions.assertEquals("test", gestalt.getConfig("db.does.not.exist", "test"))
        val hosts: List<Host> = gestalt.getConfig("db.hosts", emptyList())
        Assertions.assertEquals(3, hosts.size)
        Assertions.assertEquals("credmond", hosts[0].user)
        Assertions.assertEquals("1234", hosts[0].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", hosts[0].url)
        Assertions.assertEquals("credmond", hosts[1].user)
        Assertions.assertEquals("5678", hosts[1].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", hosts[1].url)
        Assertions.assertEquals("credmond", hosts[2].user)
        Assertions.assertEquals("9012", hosts[2].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", hosts[2].url)
        val noHosts: List<Host> = gestalt.getConfig("db.not.hosts", emptyList())
        Assertions.assertEquals(0, noHosts.size)
        val admin = gestalt.getConfig("admin", object : TypeCapture<User?>() {})!!
        Assertions.assertEquals(3, admin.users?.size)
        Assertions.assertEquals("Peter", admin.users!![0])
        Assertions.assertEquals("Kim", admin.users!![1])
        Assertions.assertEquals("Steve", admin.users!![2])
        Assertions.assertEquals(Role.LEVEL0, admin.accessRole)
        Assertions.assertTrue(admin.overrideEnabled)
        val user:User = gestalt.getConfig("employee")
        Assertions.assertEquals(1, user.users!!.size)
        Assertions.assertEquals("Janice", user.users!![0])
        Assertions.assertEquals(Role.LEVEL1, user.accessRole)
        Assertions.assertFalse(user.overrideEnabled)
        Assertions.assertEquals(
            "active", gestalt.getConfig(
                "serviceMode", TypeCapture.of(
                    String::class.java
                )
            )
        )
        Assertions.assertEquals(
            'a', gestalt.getConfig(
                "serviceMode", TypeCapture.of(
                    Char::class.java
                )
            )
        )
    }

    @Test
    @Throws(GestaltException::class)
    fun `Test Null Return Types`() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.hosts[0].password"] = "1234"
        configs["db.hosts[1].password"] = "5678"
        configs["db.hosts[2].password"] = "9012"
        configs["db.idleTimeout"] = "123"
        val defaultFileURL = KotlinGestaltIntegrationTests::class.java.classLoader.getResource("default.properties")
        val defaultFile = File(defaultFileURL.file)
        val devFileURL = KotlinGestaltIntegrationTests::class.java.classLoader.getResource("dev.properties")
        val devFile = File(devFileURL.file)
        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(FileConfigSourceBuilder.builder().setFile(defaultFile).build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build()
        gestalt.loadConfigs()
        val maxTotal: Short? = gestalt.getConfig("http.pool.maxTotal")
        Assertions.assertEquals(1000, maxTotal)

        val notExist: Short? = gestalt.getConfig("http.pool.notExist")
        Assertions.assertNull(notExist)

        try {
            gestalt.getConfig<Long>("http.pool.notExist")
            Assertions.fail("Should not reach this")
        } catch (e: GestaltException) {
            Assertions.assertEquals(
                "Failed getting config path: http.pool.notExist, for class: long\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: http.pool.notExist, for class: ObjectToken, " +
                    "during navigating to next node", e.message
            )
        }

    }

    @Test
    @Throws(GestaltException::class)
    fun `Test Null Return Types missing values`() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.hosts[0].password"] = "1234"
        configs["db.hosts[1].password"] = "5678"
        configs["db.hosts[2].password"] = "9012"
        configs["db.idleTimeout"] = "123"
        val defaultFileURL = KotlinGestaltIntegrationTests::class.java.classLoader.getResource("default.properties")
        val defaultFile = File(defaultFileURL.file)
        val devFileURL = KotlinGestaltIntegrationTests::class.java.classLoader.getResource("dev.properties")
        val devFile = File(devFileURL.file)
        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(FileConfigSourceBuilder.builder().setFile(defaultFile).build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .build()
        gestalt.loadConfigs()
        val maxTotal: Short? = gestalt.getConfig("http.pool.maxTotal")
        Assertions.assertEquals(1000, maxTotal)

        val notExist: Short? = gestalt.getConfig("http.pool.notExist")
        Assertions.assertNull(notExist)

        try {
            gestalt.getConfig<Long>("http.pool.notExist")
            Assertions.fail("Should not reach this")
        } catch (e: GestaltException) {
            Assertions.assertEquals(
                "Failed getting config path: http.pool.notExist, for class: long\n" +
                    " - level: MISSING_VALUE, message: Unable to find node matching path: http.pool.notExist, for class: ObjectToken, " +
                    "during navigating to next node", e.message
            )
        }

    }

    @Test
    @Throws(GestaltException::class)
    fun integrationTestTags() {

        val defaultFileURL = KotlinGestaltIntegrationTests::class.java.classLoader.getResource("default.properties")
        val defaultFile = File(defaultFileURL.file)
        val devFileURL = KotlinGestaltIntegrationTests::class.java.classLoader.getResource("dev.properties")
        val devFile = File(devFileURL.file)
        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(FileConfigSourceBuilder.builder().setFile(defaultFile).build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).setTags(Tags.of("env", "dev")).build())
            .build()
        gestalt.loadConfigs()
        val pool = gestalt.getConfig<HttpPool>("http.pool")
        Assertions.assertEquals(100, pool.maxTotal.toInt())
        Assertions.assertEquals(100.toShort(), gestalt.getConfig("http.pool.maxTotal"))
        Assertions.assertEquals(10L, pool.maxPerRoute)
        Assertions.assertEquals(10L, gestalt.getConfig("http.pool.maxPerRoute"))
        Assertions.assertEquals(6000, pool.validateAfterInactivity)
        Assertions.assertEquals(60000.0, pool.keepAliveTimeoutMs)
        Assertions.assertEquals(25, pool.idleTimeoutSec)
        Assertions.assertEquals(33.0f, pool.defaultWait)

        val devEnv = Tags.of("env", "dev")
        val poolTags = gestalt.getConfig<HttpPool>("http.pool", devEnv)
        Assertions.assertEquals(1000, poolTags.maxTotal.toInt())
        Assertions.assertEquals(1000, poolTags.maxTotal.toInt())
        Assertions.assertEquals(1000.toShort(), gestalt.getConfig("http.pool.maxTotal", devEnv))
        Assertions.assertEquals(50L, poolTags.maxPerRoute)
        Assertions.assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute", devEnv))
        Assertions.assertEquals(6000, poolTags.validateAfterInactivity)
        Assertions.assertEquals(60000.0, poolTags.keepAliveTimeoutMs)
        Assertions.assertEquals(25, poolTags.idleTimeoutSec)
        Assertions.assertEquals(33.0f, poolTags.defaultWait)
    }

    @Test
    @Throws(GestaltException::class)
    @SuppressWarnings("LongMethod")
    fun integrationTestEnvVars() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.hosts[0].password"] = "1234"
        configs["db.hosts[1].password"] = "5678"
        configs["db.hosts[2].password"] = "9012"
        val defaultFileURL = KotlinGestaltIntegrationTests::class.java.classLoader.getResource("default.properties")
        val defaultFile = File(defaultFileURL.file)
        val devFileURL = KotlinGestaltIntegrationTests::class.java.classLoader.getResource("dev.properties")
        val devFile = File(devFileURL.file)
        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(FileConfigSourceBuilder.builder().setFile(defaultFile).build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(EnvironmentConfigSourceBuilder.builder().build())
            .build()
        gestalt.loadConfigs()
        val pool = gestalt.getConfig("http.pool", HttpPool::class.java)
        Assertions.assertEquals(1000, pool.maxTotal.toInt())
        Assertions.assertEquals(1000.toShort(), gestalt.getConfig("http.pool.maxTotal", Short::class.java))
        Assertions.assertEquals(50L, pool.maxPerRoute)
        Assertions.assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute", Long::class.java))
        Assertions.assertEquals(6000, pool.validateAfterInactivity)
        Assertions.assertEquals(60000.0, pool.keepAliveTimeoutMs)
        Assertions.assertEquals(25, pool.idleTimeoutSec)
        Assertions.assertEquals(33.0f, pool.defaultWait)
        var startTime = System.nanoTime()
        gestalt.getConfig("db", DataBase::class.java)
        val timeTaken = System.nanoTime() - startTime
        startTime = System.nanoTime()
        val db = gestalt.getConfig("db", DataBase::class.java)
        val cacheTimeTaken = System.nanoTime() - startTime

        // not really a great test for ensuring we are hitting a cache
        Assertions.assertTrue(timeTaken > cacheTimeTaken)
        Assertions.assertEquals(600, db.connectionTimeout)
        Assertions.assertEquals(600, gestalt.getConfig("db.connectionTimeout"))
        Assertions.assertEquals(123, db.idleTimeout)
        Assertions.assertEquals(60000.0f, db.maxLifetime)
        Assertions.assertTrue(db.isEnabled!!)
        Assertions.assertTrue(gestalt.getConfig("db.isEnabled", true))
        Assertions.assertEquals(3, db.hosts!!.size)
        Assertions.assertEquals("credmond", db.hosts!![0].user)
        Assertions.assertEquals("credmond", gestalt.getConfig("db.hosts[0].user", "test"))
        Assertions.assertEquals("1234", db.hosts!![0].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", db.hosts!![0].url)
        Assertions.assertEquals("credmond", db.hosts!![1].user)
        Assertions.assertEquals("5678", db.hosts!![1].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", db.hosts!![1].url)
        Assertions.assertEquals("credmond", db.hosts!![2].user)
        Assertions.assertEquals("9012", db.hosts!![2].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", db.hosts!![2].url)
        Assertions.assertEquals("test", gestalt.getConfig("db.does.not.exist", "test"))
        val hosts: List<Host> = gestalt.getConfig("db.hosts", emptyList())
        Assertions.assertEquals(3, hosts.size)
        Assertions.assertEquals("credmond", hosts[0].user)
        Assertions.assertEquals("1234", hosts[0].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", hosts[0].url)
        Assertions.assertEquals("credmond", hosts[1].user)
        Assertions.assertEquals("5678", hosts[1].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", hosts[1].url)
        Assertions.assertEquals("credmond", hosts[2].user)
        Assertions.assertEquals("9012", hosts[2].password)
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", hosts[2].url)
        val noHosts: List<Host> = gestalt.getConfig("db.not.hosts", emptyList())
        Assertions.assertEquals(0, noHosts.size)
        val admin: User = gestalt.getConfig("admin")!!
        Assertions.assertEquals(3, admin.users?.size)
        Assertions.assertEquals("Peter", admin.users!![0])
        Assertions.assertEquals("Kim", admin.users!![1])
        Assertions.assertEquals("Steve", admin.users!![2])
        Assertions.assertEquals(Role.LEVEL0, admin.accessRole)
        Assertions.assertTrue(admin.overrideEnabled)
        val user = gestalt.getConfig<User>("employee")
        Assertions.assertEquals(1, user.users!!.size)
        Assertions.assertEquals("Janice", user.users!![0])
        Assertions.assertEquals(Role.LEVEL1, user.accessRole)
        Assertions.assertFalse(user.overrideEnabled)
        Assertions.assertEquals(
            "active", gestalt.getConfig(
                "serviceMode", TypeCapture.of(
                    String::class.java
                )
            )
        )
        Assertions.assertEquals(
            'a', gestalt.getConfig(
                "serviceMode", TypeCapture.of(
                    Char::class.java
                )
            )
        )
        val booking = gestalt.getConfig(
            "subservice.booking", TypeCapture.of(
                SubService::class.java
            )
        )
        Assertions.assertTrue(booking.isEnabled)
        Assertions.assertEquals("https://dev.booking.host.name", booking.service!!.host)
        Assertions.assertEquals(443, booking.service!!.port)
        Assertions.assertEquals("booking", booking.service!!.path)
    }

    enum class Role {
        LEVEL0, LEVEL1
    }

    class HttpPool {
        var maxTotal: Short = 0
        var maxPerRoute: Long = 0
        var validateAfterInactivity = 0
        var keepAliveTimeoutMs = 6000.0
        var idleTimeoutSec = 10
        var defaultWait = 33.0f
    }

    class Host {
        var user: String? = null
        var url: String? = null
        var password: String? = null
    }

    class DataBase {
        var hosts: List<Host>? = null
        var connectionTimeout = 0
        var idleTimeout: Int? = null
        var maxLifetime = 0f
        var isEnabled: Boolean? = true
    }

    data class User(
        var users: Array<String>? = null,
        var overrideEnabled: Boolean = false,
        var accessRole: Role? = null
    )

    data class SubService(
        var isEnabled: Boolean = false,
        var service: Connection? = null
    )

    class Connection {
        var host: String? = null
        var port = 0
        var path: String? = null
    }
}
