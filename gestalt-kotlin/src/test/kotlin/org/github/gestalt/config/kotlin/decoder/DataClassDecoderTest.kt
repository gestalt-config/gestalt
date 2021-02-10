package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderRegistry
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.exceptions.ConfigurationException
import org.github.gestalt.config.kotlin.reflect.kTypeCaptureOf
import org.github.gestalt.config.kotlin.test.classes.DBInfo
import org.github.gestalt.config.kotlin.test.classes.DBInfoNoDefault
import org.github.gestalt.config.kotlin.test.classes.DBInfoNoDefaultOptional
import org.github.gestalt.config.lexer.PathLexer
import org.github.gestalt.config.lexer.SentenceLexer
import org.github.gestalt.config.node.*
import org.github.gestalt.config.reflect.TypeCapture
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class DataClassDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer = PathLexer()
    var registry: DecoderRegistry? = null

    @BeforeEach
    @Throws(ConfigurationException::class)
    fun setup() {
        configNodeService = ConfigNodeManager()
        registry = DecoderRegistry(
            Arrays.asList(
                LongDecoder(), IntegerDecoder(), StringDecoder(),
                DataClassDecoder(), FloatDecoder()
            ), configNodeService, lexer
        )
    }

    @Test
    fun name() {
        val decoder = DataClassDecoder()
        Assertions.assertEquals("DataClass", decoder.name())
    }

    @Test
    fun priority() {
        val decoder = DataClassDecoder()
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority())
    }

    @Test
    fun matches() {
        val decoder = DataClassDecoder()
        Assertions.assertTrue(decoder.matches(kTypeCaptureOf<DBInfo>()))
        Assertions.assertTrue(decoder.matches(kTypeCaptureOf<DBInfoNoDefault>()))
        Assertions.assertFalse(decoder.matches(TypeCapture.of(DBInfoNoDefault::class.java)))
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long::class.java)))
        Assertions.assertFalse(decoder.matches(object : TypeCapture<Long?>() {}))
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long::class.javaPrimitiveType)))
        Assertions.assertFalse(decoder.matches(TypeCapture.of(String::class.java)))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.matches(object : TypeCapture<List<Long?>?>() {}))
        Assertions.assertFalse(decoder.matches(object : TypeCapture<Map<String?, Long?>?>() {}))
    }

    @Test
    fun decode() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode("100")
        configs["uri"] = LeafNode("mysql.com")
        configs["password"] = LeafNode("pass")

        val validate = decoder.decode("db.host", MapNode(configs), kTypeCaptureOf<DBInfo>(), registry!!)
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        val results: DBInfo = validate.results() as DBInfo
        Assertions.assertEquals(100, results.port)
        Assertions.assertEquals("pass", results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }

    @Test
    fun `decode Missing With Default`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode("100")
        configs["uri"] = LeafNode("mysql.com")

        val validate = decoder.decode("db.host", MapNode(configs), kTypeCaptureOf<DBInfo>(), registry!!)
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertEquals(
            "Unable to find object node for path: db.host.password, at token: ObjectToken",
            validate.errors[0].description()
        )

        val results: DBInfo = validate.results() as DBInfo
        Assertions.assertEquals(100, results.port)
        Assertions.assertEquals("password", results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }

    @Test
    fun `decode Missing All Members`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        val validate = decoder.decode("db.host", MapNode(configs), kTypeCaptureOf<DBInfo>(), registry!!)
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertEquals(3, validate.errors.size)

        val results: DBInfo = validate.results() as DBInfo
        Assertions.assertEquals(0, results.port)
        Assertions.assertEquals("password", results.password)
        Assertions.assertEquals("mysql:URI", results.uri)
    }

    @Test
    fun `decode Missing Optional Member Null`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode("100")
        configs["uri"] = LeafNode("mysql.com")

        val validate = decoder.decode("db.host", MapNode(configs), kTypeCaptureOf<DBInfoNoDefaultOptional>(), registry!!)
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertEquals(1, validate.errors.size)
        Assertions.assertEquals(
            "Unable to find object node for path: db.host.password, at token: ObjectToken",
            validate.errors[0].description()
        )

        val results: DBInfoNoDefaultOptional = validate.results() as DBInfoNoDefaultOptional
        Assertions.assertEquals(100, results.port)
        Assertions.assertNull(results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }

    @Test
    fun `decode Missing Non Optional Member`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode("100")
        configs["uri"] = LeafNode("mysql.com")

        val validate = decoder.decode("db.host", MapNode(configs), kTypeCaptureOf<DBInfoNoDefault>(), registry!!)
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertEquals(2, validate.errors.size)
        Assertions.assertEquals(
            "Unable to find object node for path: db.host.password, at token: ObjectToken",
            validate.errors[0].description()
        )
        Assertions.assertEquals(
            "Data Class: DBInfoNoDefault, can not be constructed. Missing required members [password], on path: db.host",
            validate.errors[1].description()
        )
    }

    @Test
    fun `decode failed bad Int`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode("abc")
        configs["uri"] = LeafNode("mysql.com")
        configs["password"] = LeafNode("pass")

        val validate = decoder.decode("db.host", MapNode(configs), kTypeCaptureOf<DBInfo>(), registry!!)
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertEquals(2, validate.errors.size)
        Assertions.assertEquals(
            "Unable to parse a number on Path: db.host.port, from node: LeafNode{value='abc'} attempting to decode kInt",
            validate.errors[0].description()
        )
        Assertions.assertEquals(
            "Unable to decode node matching path: db.host.port, for class: class kotlin.Int",
            validate.errors[1].description()
        )

        val results: DBInfo = validate.results() as DBInfo
        Assertions.assertEquals(0, results.port)
        Assertions.assertEquals("pass", results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }

    @Test
    fun `decode failed null leaf value`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode(null)
        configs["uri"] = LeafNode("mysql.com")
        configs["password"] = LeafNode("pass")

        val validate = decoder.decode("db.host", MapNode(configs), kTypeCaptureOf<DBInfo>(), registry!!)
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertEquals(2, validate.errors.size)
        Assertions.assertEquals(
            "Leaf on path: db.host.port, missing value, LeafNode{value='null'} attempting to decode kInt",
            validate.errors[0].description()
        )
        Assertions.assertEquals(
            "Unable to decode node matching path: db.host.port, for class: class kotlin.Int",
            validate.errors[1].description()
        )

        val results: DBInfo = validate.results() as DBInfo
        Assertions.assertEquals(0, results.port)
        Assertions.assertEquals("pass", results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }
}
