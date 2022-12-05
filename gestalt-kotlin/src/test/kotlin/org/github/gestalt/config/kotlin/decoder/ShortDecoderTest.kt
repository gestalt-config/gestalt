package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderRegistry
import org.github.gestalt.config.entity.ValidationLevel
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.kotlin.reflect.kTypeCaptureOf
import org.github.gestalt.config.lexer.SentenceLexer
import org.github.gestalt.config.node.ConfigNodeService
import org.github.gestalt.config.node.LeafNode
import org.github.gestalt.config.path.mapper.CamelCasePathMapper
import org.github.gestalt.config.path.mapper.StandardPathMapper
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.utils.ValidateOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*

internal class ShortDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
    }

    @Test
    fun name() {
        val decoder = ShortDecoder()
        Assertions.assertEquals("kShort", decoder.name())
    }

    @Test
    fun matches() {
        val decoder = ShortDecoder()
        Assertions.assertTrue(decoder.matches(kTypeCaptureOf<Short>()))
        Assertions.assertFalse(decoder.matches(object : TypeCapture<Short?>() {}))
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Short::class.java)))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decode() {
        val decoder = ShortDecoder()
        val validate: ValidateOf<Short> = decoder.decode(
            "db.port", LeafNode("124"), TypeCapture.of(
                Short::class.java
            ),
            DecoderRegistry(listOf(decoder), configNodeService, lexer, listOf(StandardPathMapper(), CamelCasePathMapper()))
        )
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        Assertions.assertEquals(124.toShort(), validate.results() as Short)
        Assertions.assertEquals(0, validate.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun `not A Short`() {
        val decoder = ShortDecoder()
        val validate: ValidateOf<Short> = decoder.decode(
            "db.port", LeafNode("12s4"), TypeCapture.of(
                Short::class.java
            ),
            DecoderRegistry(listOf(decoder), configNodeService, lexer, listOf(StandardPathMapper(), CamelCasePathMapper()))
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, validate.errors[0].level())
        Assertions.assertEquals(
            "Unable to parse a number on Path: db.port, from node: LeafNode{value='12s4'} " +
                "attempting to decode kShort",
            validate.errors[0].description()
        )
    }

    @Test
    @Throws(GestaltException::class)
    fun `not A Short Too Large`() {
        val decoder = ShortDecoder()
        val validate: ValidateOf<Short> = decoder.decode(
            "db.port", LeafNode("12345678901234567890123456789012345678901234567890123456789"),
            TypeCapture.of(Short::class.java), DecoderRegistry(listOf(decoder), configNodeService, lexer,
                listOf(StandardPathMapper(), CamelCasePathMapper()))
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, validate.errors[0].level())
        Assertions.assertEquals(
            "Unable to decode a number on path: db.port, from node: " +
                "LeafNode{value='12345678901234567890123456789012345678901234567890123456789'} attempting to decode kShort",
            validate.errors[0].description()
        )
    }
}
