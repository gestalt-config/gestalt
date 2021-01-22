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

internal class LongDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
    }

    @Test
    fun name() {
        val decoder = LongDecoder()
        Assertions.assertEquals("kLong", decoder.name())
    }

    @Test
    fun matches() {
        val decoder = LongDecoder()
        Assertions.assertTrue(decoder.matches(kTypeCaptureOf<Long>()))
        Assertions.assertFalse(decoder.matches(object : TypeCapture<Long?>() {}))
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long::class.java)))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decode() {
        val longDecoder = LongDecoder()
        val validate: ValidateOf<Long> = longDecoder.decode(
            "db.port", LeafNode("124"), TypeCapture.of(
                Long::class.java
            ),
            DecoderRegistry(listOf(longDecoder), configNodeService, lexer)
        )
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        Assertions.assertEquals(124L, validate.results())
        Assertions.assertEquals(0, validate.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun notALong() {
        val longDecoder = LongDecoder()
        val validate: ValidateOf<Long> = longDecoder.decode(
            "db.port", LeafNode("12s4"), TypeCapture.of(
                Long::class.java
            ),
            DecoderRegistry(listOf(longDecoder), configNodeService, lexer)
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, validate.errors[0].level())
        Assertions.assertEquals(
            "Unable to parse a number on Path: db.port, from node: " +
                "LeafNode{value='12s4'} attempting to decode kLong",
            validate.errors[0].description()
        )
    }

    @Test
    @Throws(GestaltException::class)
    fun notALongTooLarge() {
        val decoder = LongDecoder()
        val validate: ValidateOf<Long> = decoder.decode(
            "db.port", LeafNode("12345678901234567890123456789012345678901234567890123456"),
            TypeCapture.of(Long::class.java), DecoderRegistry(listOf(decoder), configNodeService, lexer)
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, validate.errors[0].level())
        Assertions.assertEquals(
            "Unable to decode a number on path: db.port, from node: " +
                "LeafNode{value='12345678901234567890123456789012345678901234567890123456'} attempting to decode kLong",
            validate.errors[0].description()
        )
    }
}
