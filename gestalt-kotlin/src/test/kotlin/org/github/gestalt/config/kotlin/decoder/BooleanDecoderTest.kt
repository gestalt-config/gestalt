package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderRegistry
import org.github.gestalt.config.decoder.LeafDecoder
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.entity.ValidationLevel
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.kotlin.reflect.kTypeCaptureOf
import org.github.gestalt.config.lexer.SentenceLexer
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.node.ConfigNodeService
import org.github.gestalt.config.node.LeafNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.utils.ValidateOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import kotlin.reflect.typeOf

internal class BooleanDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
    }

    @Test
    fun name() {
        val decoder = BooleanDecoder()
        Assertions.assertEquals("kBoolean", decoder.name())
    }

    @ExperimentalStdlibApi
    @Test
    fun matches() {
        val decoder = BooleanDecoder()
        Assertions.assertTrue(decoder.matches(kTypeCaptureOf<Boolean>()))
        Assertions.assertTrue(decoder.matches(KTypeCapture.of<Boolean>(typeOf<Boolean>())))
        Assertions.assertFalse(decoder.matches(object : TypeCapture<Boolean?>() {}))
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Boolean::class.java)))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decode() {
        val decoder = BooleanDecoder()
        val validate: ValidateOf<Boolean> = decoder.decode(
            "db.enabled", LeafNode("true"), TypeCapture.of(
                Int::class.java
            ),
            DecoderRegistry(listOf(decoder), configNodeService, lexer)
        )
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        Assertions.assertTrue(validate.results())
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeFalse() {
        val decoder = BooleanDecoder()
        val validate: ValidateOf<Boolean> = decoder.decode(
            "db.enabled", LeafNode("false"), TypeCapture.of(
                Int::class.java
            ),
            DecoderRegistry(listOf(decoder), configNodeService, lexer)
        )
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        Assertions.assertFalse(validate.results())
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeFalseNull() {
        val decoder = BooleanDecoder()
        val validate: ValidateOf<Boolean> = decoder.decode(
            "db.enabled", LeafNode(null), TypeCapture.of(
                Int::class.java
            ),
            DecoderRegistry(listOf(decoder), configNodeService, lexer)
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, validate.errors[0].level())
        Assertions.assertEquals(
            "Leaf on path: db.enabled, missing value, LeafNode{value='null'} attempting to decode kBoolean",
            validate.errors[0].description()
        )
    }
}
