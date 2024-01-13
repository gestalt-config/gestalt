package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderContext
import org.github.gestalt.config.decoder.DecoderRegistry
import org.github.gestalt.config.entity.ValidationLevel
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.kotlin.reflect.kTypeCaptureOf
import org.github.gestalt.config.lexer.SentenceLexer
import org.github.gestalt.config.node.ConfigNodeService
import org.github.gestalt.config.node.LeafNode
import org.github.gestalt.config.path.mapper.DotNotationPathMapper
import org.github.gestalt.config.path.mapper.StandardPathMapper
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.github.gestalt.config.utils.GResultOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*

internal class DoubleDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null
    var decoderService: DecoderRegistry? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
        decoderService = DecoderRegistry(
            listOf(DoubleDecoder()), configNodeService, lexer, listOf(
                StandardPathMapper(),
                DotNotationPathMapper()
            )
        )
    }

    @Test
    fun name() {
        val decoder = DoubleDecoder()
        Assertions.assertEquals("kDouble", decoder.name())
    }

    @Test
    fun canDecode() {
        val decoder = DoubleDecoder()
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Double>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), object : TypeCapture<Double?>() {}))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), TypeCapture.of(Double::class.java)))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeDouble() {
        val doubleDecoder = DoubleDecoder()
        val result: GResultOf<Double> = doubleDecoder.decode(
            "db.port", Tags.of(),
            LeafNode("124.5"),
            TypeCapture.of(
                Double::class.java
            ),
            DecoderContext(decoderService, null),
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertFalse(result.hasErrors())
        Assertions.assertEquals(124.5, result.results())
        Assertions.assertEquals(0, result.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeDoubleType() {
        val doubleDecoder = DoubleDecoder()
        val result: GResultOf<Double> = doubleDecoder.decode(
            "db.port", Tags.of(),
            LeafNode("124.5"),
            object : TypeCapture<Double?>() {},
            DecoderContext(decoderService, null),
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertFalse(result.hasErrors())
        Assertions.assertEquals(124.5, result.results())
        Assertions.assertEquals(0, result.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeDouble2() {
        val doubleDecoder = DoubleDecoder()
        val result: GResultOf<Double> = doubleDecoder.decode(
            "db.port", Tags.of(),
            LeafNode("124"),
            TypeCapture.of(
                Double::class.java
            ),
            DecoderContext(decoderService, null),
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertFalse(result.hasErrors())
        Assertions.assertEquals(124.0, result.results())
        Assertions.assertEquals(0, result.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun notADouble() {
        val doubleDecoder = DoubleDecoder()
        val result: GResultOf<Double> = doubleDecoder.decode(
            "db.port", Tags.of(),
            LeafNode("12s4"),
            TypeCapture.of(
                Double::class.java
            ),
            DecoderContext(decoderService, null),
        )
        Assertions.assertFalse(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertNull(result.results())
        Assertions.assertNotNull(result.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, result.errors[0].level())
        Assertions.assertEquals(
            "Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode kDouble",
            result.errors[0].description()
        )
    }
}
