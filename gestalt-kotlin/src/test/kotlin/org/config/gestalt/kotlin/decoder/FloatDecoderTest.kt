package org.config.gestalt.kotlin.decoder

import org.config.gestalt.decoder.DecoderRegistry
import org.config.gestalt.entity.ValidationLevel
import org.config.gestalt.exceptions.GestaltException
import org.config.gestalt.kotlin.reflect.kTypeCaptureOf
import org.config.gestalt.lexer.SentenceLexer
import org.config.gestalt.node.ConfigNodeService
import org.config.gestalt.node.LeafNode
import org.config.gestalt.reflect.TypeCapture
import org.config.gestalt.utils.ValidateOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*

internal class FloatDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
    }

    @Test
    fun name() {
        val decoder = FloatDecoder()
        Assertions.assertEquals("Float", decoder.name())
    }

    @Test
    fun matches() {
        val decoder = FloatDecoder()
        Assertions.assertTrue(decoder.matches(kTypeCaptureOf<Float>()))
        Assertions.assertFalse(decoder.matches(object : TypeCapture<Float?>() {}))
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Float::class.java)))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeFloat() {
        val floatDecoder = FloatDecoder()
        val validate: ValidateOf<Float> = floatDecoder.decode(
            "db.timeout", LeafNode("124.5"), TypeCapture.of(
                Float::class.java
            ),
            DecoderRegistry(listOf(floatDecoder), configNodeService, lexer)
        )
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        Assertions.assertEquals(124.5f, validate.results())
        Assertions.assertEquals(0, validate.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeFloat2() {
        val floatDecoder = FloatDecoder()
        val validate: ValidateOf<Float> = floatDecoder.decode(
            "db.timeout", LeafNode("124"), TypeCapture.of(
                Float::class.java
            ),
            DecoderRegistry(listOf(floatDecoder), configNodeService, lexer)
        )
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        Assertions.assertEquals(124f, validate.results())
        Assertions.assertEquals(0, validate.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun notAFloat() {
        val floatDecoder = FloatDecoder()
        val validate: ValidateOf<Float> = floatDecoder.decode(
            "db.timeout", LeafNode("12s4"), TypeCapture.of(
                Float::class.java
            ),
            DecoderRegistry(listOf(floatDecoder), configNodeService, lexer)
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, validate.errors[0].level())
        Assertions.assertEquals(
            "Unable to parse a number on Path: db.timeout, from node: LeafNode{value='12s4'} " +
                "attempting to decode Float",
            validate.errors[0].description()
        )
    }
}
