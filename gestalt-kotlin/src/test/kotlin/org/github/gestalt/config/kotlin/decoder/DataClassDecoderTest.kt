package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderContext
import org.github.gestalt.config.decoder.DecoderRegistry
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.exceptions.GestaltConfigurationException
import org.github.gestalt.config.kotlin.reflect.kTypeCaptureOf
import org.github.gestalt.config.kotlin.test.classes.*
import org.github.gestalt.config.lexer.PathLexer
import org.github.gestalt.config.lexer.SentenceLexer
import org.github.gestalt.config.node.*
import org.github.gestalt.config.path.mapper.StandardPathMapper
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class DataClassDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer = PathLexer()
    private var decoderService: DecoderRegistry? = null

    @BeforeEach
    @Throws(GestaltConfigurationException::class)
    fun setup() {
        configNodeService = ConfigNodeManager()
        decoderService = DecoderRegistry(
            listOf(
                LongDecoder(), IntegerDecoder(), StringDecoder(),
                DataClassDecoder(), FloatDecoder()
            ), configNodeService, lexer, listOf(StandardPathMapper())
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
    fun canDecode() {
        val decoder = DataClassDecoder()
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<DBInfo>()))
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<DBInfoNoDefault>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), TypeCapture.of(DBInfoNoDefault::class.java)))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), TypeCapture.of(Long::class.java)))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), object : TypeCapture<Long?>() {}))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), TypeCapture.of(Long::class.javaPrimitiveType)))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), TypeCapture.of(String::class.java)))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), object : TypeCapture<List<Long?>?>() {}))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), object : TypeCapture<Map<String?, Long?>?>() {}))
    }

    @Test
    fun decode() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode("100")
        configs["uri"] = LeafNode("mysql.com")
        configs["password"] = LeafNode("pass")

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfo>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertFalse(result.hasErrors())
        val results: DBInfo = result.results() as DBInfo
        Assertions.assertEquals(100, results.port)
        Assertions.assertEquals("pass", results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }

    @Test
    fun `decode wrong node type`() {
        val decoder = DataClassDecoder()
        val configs = LeafNode("pass")

        val result = decoder.decode(
            "db.host", Tags.of(),
            configs,
            kTypeCaptureOf<DBInfo>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertFalse(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals(
            "Expected a map node on path: db.host, received node type : LEAF",
            result.errors[0].description()
        )
    }

    @Test
    fun `decode wrong TypeCapture`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode("100")
        configs["uri"] = LeafNode("mysql.com")
        configs["password"] = LeafNode("pass")

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            TypeCapture.of(DBInfo::class.java),
            DecoderContext(decoderService, null)
        )
        Assertions.assertFalse(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals(
            "Data Class: org.github.gestalt.config.kotlin.test.classes.DBInfo, can not be constructed on path: db.host",
            result.errors[0].description()
        )
    }

    @Test
    fun `decode Missing With Default`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode("100")
        configs["uri"] = LeafNode("mysql.com")

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfo>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals(
            "Unable to find node matching path: db.host.password, for class: ObjectToken, during navigating to next node",
            result.errors[0].description()
        )

        val results: DBInfo = result.results() as DBInfo
        Assertions.assertEquals(100, results.port)
        Assertions.assertEquals("password", results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }

    @Test
    fun `decode Missing Required`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode("100")
        configs["uri"] = LeafNode("mysql.com")

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfoRequired>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertFalse(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals(
            "Unable to find node matching path: db.host.password, for class: ObjectToken, during navigating to next node",
            result.errors[0].description()
        )
    }

    @Test
    fun `decode Missing All Members`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfo>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals(3, result.errors.size)

        val results: DBInfo = result.results() as DBInfo
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

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfoNoDefaultOptional>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals(1, result.errors.size)
        Assertions.assertEquals(
            "Unable to find node matching path: db.host.password, for class: ObjectToken, during navigating to next node",
            result.errors[0].description()
        )

        val results: DBInfoNoDefaultOptional = result.results() as DBInfoNoDefaultOptional
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

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfoNoDefault>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertFalse(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals(2, result.errors.size)
        Assertions.assertEquals(
            "Unable to find node matching path: db.host.password, for class: ObjectToken, during navigating to next node",
            result.errors[0].description()
        )
        Assertions.assertEquals(
            "Data Class: DBInfoNoDefault, can not be constructed. Missing required members [password], on path: db.host",
            result.errors[1].description()
        )
    }

    @Test
    fun `decode failed bad Int`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["port"] = LeafNode("abc")
        configs["uri"] = LeafNode("mysql.com")
        configs["password"] = LeafNode("pass")

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfo>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals(2, result.errors.size)
        Assertions.assertEquals(
            "Unable to parse a number on Path: db.host.port, from node: LeafNode{value='abc'} attempting to decode kInt",
            result.errors[0].description()
        )
        Assertions.assertEquals(
            "Unable to find node matching path: db.host.port, for class: class kotlin.Int, during data decoding",
            result.errors[1].description()
        )

        val results: DBInfo = result.results() as DBInfo
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

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfo>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals(2, result.errors.size)
        Assertions.assertEquals(
            "Leaf on path: db.host.port, has no value attempting to decode kInt",
            result.errors[0].description()
        )
        Assertions.assertEquals(
            "Unable to find node matching path: db.host.port, for class: class kotlin.Int, during data decoding",
            result.errors[1].description()
        )

        val results: DBInfo = result.results() as DBInfo
        Assertions.assertEquals(0, results.port)
        Assertions.assertEquals("pass", results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }

    @Test
    fun `decode with annotations`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["channel"] = LeafNode("100")
        configs["uri"] = LeafNode("mysql.com")
        configs["password"] = LeafNode("pass")

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfoAnnotation>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertFalse(result.hasErrors())

        val results: DBInfoAnnotation = result.results() as DBInfoAnnotation
        Assertions.assertEquals(100, results.port)
        Assertions.assertEquals("pass", results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }

    @Test
    fun `decode with annotations default`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["uri"] = LeafNode("mysql.com")
        configs["password"] = LeafNode("pass")

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfoAnnotation>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals(1, result.errors.size)
        Assertions.assertEquals(
            "Unable to find node matching path: db.host.channel, for class: ObjectToken, during navigating to next node",
            result.errors[0].description()
        )

        val results: DBInfoAnnotation = result.results() as DBInfoAnnotation
        Assertions.assertEquals(1234, results.port)
        Assertions.assertEquals("pass", results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }

    @Test
    fun `decode with annotations long path`() {
        val decoder = DataClassDecoder()
        val configs: MutableMap<String, ConfigNode> = HashMap()
        configs["channel"] = MapNode(mapOf("port" to LeafNode("100")))
        configs["uri"] = LeafNode("mysql.com")
        configs["password"] = LeafNode("pass")

        val result = decoder.decode(
            "db.host", Tags.of(),
            MapNode(configs),
            kTypeCaptureOf<DBInfoAnnotationLong>(),
            DecoderContext(decoderService, null)
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertFalse(result.hasErrors())

        val results: DBInfoAnnotationLong = result.results() as DBInfoAnnotationLong
        Assertions.assertEquals(100, results.port)
        Assertions.assertEquals("pass", results.password)
        Assertions.assertEquals("mysql.com", results.uri)
    }
}
